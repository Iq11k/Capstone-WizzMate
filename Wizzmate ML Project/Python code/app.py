import numpy as np
import pandas as pd
import tensorflow as tf
from tensorflow.keras.layers import Input, Embedding, Flatten, Dot, Add
from tensorflow.keras.models import Model, load_model
from flask import Flask, request, jsonify
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import googlemaps

app = Flask(__name__)

# Load dataset
rating = pd.read_csv('../data/rating_clean.csv')
flights_data = pd.read_csv('../data/flightsCapstone_cleaned.csv')
merge_data=  pd.read_csv('../data/merge_data.csv')

def filter_places(city=None, categories=None):
    """
    Filter places based on city and categories.

    Args:
        city: City name to filter places (optional).
        categories: List of categories to filter places (optional).

    Returns:
        Filtered DataFrame.
    """
    print(f"Filtering places - City: {city}, Categories: {categories}")

    filtered = merge_data.copy()
    if city:
        filtered = filtered[filtered['City'].str.contains(city, case=False, na=False)]

    if categories:
        print(f"Categories before filtering: {filtered['Category'].unique()}")
        filtered = filtered[filtered['Category'].isin(categories)]
        print(f"Categories after filtering: {filtered['Category'].unique()}")
        print(f"Number of places after category filtering: {len(filtered)}")

    return filtered

# Function to map city names to full airport names
def map_city_to_airport(city):
    """Map input city to full airport name"""
    airport_mapping = {
        'Jakarta': 'Bandar Udara Internasional Soekarno Hatta',
        'Yogyakarta': 'Bandar Udara Internasional Yogyakarta',
        'Semarang': 'Bandar Udara Jenderal Ahmad Yani',
        'Surabaya': 'Bandar Udara Internasional Juanda',
        'Singapore': 'Bandar Udara Internasional Changi Singapura'
    }
    return airport_mapping.get(city, city)

# Function to filter flights based on departure city, destination city, and optional budget
def filter_flights(departure_city, destination_city, max_budget=None):
    """Filters flights based on departure city, destination city, and maximum budget"""
    # Convert city names to full airport names
    departure_airport = map_city_to_airport(departure_city)
    destination_airport = map_city_to_airport(destination_city)

    # Filter flights based on the given criteria
    filtered_flights = flights_data[
        (flights_data['departure_airport_name'] == departure_airport) &
        (flights_data['arrival_airport_name'] == destination_airport)
    ]

    if max_budget:
        filtered_flights = filtered_flights[filtered_flights['price'] <= max_budget]

    return filtered_flights.sort_values(by='price')

# Prepare the data for the CF model
user_ids = rating['User_Id'].unique().tolist()
place_ids = merge_data['Place_Id'].unique().tolist()

user_id_to_index = {user_id: index for index, user_id in enumerate(user_ids)}
place_id_to_index = {place_id: index for index, place_id in enumerate(place_ids)}

rating['User_Index'] = rating['User_Id'].map(user_id_to_index)
rating['Place_Index'] = rating['Place_Id'].map(place_id_to_index)

num_users = len(user_ids)
num_places = len(place_ids)
embedding_size = 50  # Size of the embedding vectors

# Define the collaborative filtering model
user_input = Input(shape=(1,))
user_embedding = Embedding(num_users, embedding_size, embeddings_regularizer=tf.keras.regularizers.l2(1e-6))(user_input)
user_vec = Flatten()(user_embedding)

place_input = Input(shape=(1,))
place_embedding = Embedding(num_places, embedding_size, embeddings_regularizer=tf.keras.regularizers.l2(1e-6))(place_input)
place_vec = Flatten()(place_embedding)

dot_product = Dot(axes=1)([user_vec, place_vec])

user_bias = Embedding(num_users, 1)(user_input)
user_bias = Flatten()(user_bias)

place_bias = Embedding(num_places, 1)(place_input)
place_bias = Flatten()(place_bias)

prediction = Add()([dot_product, user_bias, place_bias])

cf_model = Model([user_input, place_input], prediction)
cf_model.compile(optimizer='adam', loss='mean_squared_error')

# Always load or train the model
def load_or_train_cf_model():
    model_path = '../model/cf_model.h5'
    
    try:
        # Try loading the pre-trained model
        cf_model = load_model(model_path)
    except:
        # If model does not exist, train it
        print("Model not found, training a new model...")
        cf_model.fit(
            [rating['User_Index'], rating['Place_Index']],
            rating['Place_Ratings'],
            epochs=20,
            verbose=1
        )
        # Save the model after training
        cf_model.save(model_path)  # Save the model for future use
    return cf_model

# Example of calling the function
cf_model = load_or_train_cf_model()

# Define the prediction function
def predict_ratings(user_id, place_ids):
    user_index = user_id_to_index[user_id]
    place_indices = [place_id_to_index[place_id] for place_id in place_ids if place_id in place_id_to_index]
    predictions = cf_model.predict([np.array([user_index] * len(place_indices)), np.array(place_indices)])
    return predictions.flatten()

# CBF Vectorizer and Cosine Similarity (Global variables)
vectorizer = TfidfVectorizer(stop_words='english')
cosine_sim = None

def calculate_cbf_scores(filtered_places):
    """
    Calculate content-based filtering (CBF) scores for the places based on categories or descriptions.
    """
    global cosine_sim  # Use the global variable for cosine similarity

    # Check if filtered_places is empty
    if filtered_places.empty:
        print("Warning: No places found after filtering!")
        return pd.DataFrame(columns=['Place_Id', 'name', 'category', 'similarity_score'])

    # Ensure 'Features' column creation works
    filtered_places['Features'] = filtered_places['Place_Name'] + ' ' + filtered_places['Category']

    try:
        # Vektorisasi fitur menggunakan TF-IDF
        tfidf_matrix = vectorizer.fit_transform(filtered_places['Features'])
        cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)  # Calculate cosine similarity
    except ValueError as e:
        print(f"Error in vectorization: {e}")
        return pd.DataFrame(columns=['Place_Id', 'name', 'category', 'similarity_score'])

    # For each place, recommend places with highest similarity scores (top 10)
    recommendations = []
    for idx in range(len(filtered_places)):
        num_similar = min(10, len(filtered_places))  # Avoid out-of-bounds indices
        similar_indices = cosine_sim[idx].argsort()[-(num_similar - 1):][::-1]  # Get top N similar places

        for similar_idx in similar_indices:
            if similar_idx != idx:  # Avoid recommending the same place
                recommendations.append({
                    'Place_Id': filtered_places.iloc[similar_idx]['Place_Id'],
                    'name': filtered_places.iloc[similar_idx]['Place_Name'],
                    'category': filtered_places.iloc[similar_idx]['Category'],
                    'similarity_score': cosine_sim[idx][similar_idx]
                })

    # If no recommendations found
    if not recommendations:
        print("No recommendations could be generated!")
        return pd.DataFrame(columns=['Place_Id', 'name', 'category', 'similarity_score'])

    return pd.DataFrame(recommendations)

# Function to calculate distance using Google Maps API# Function to calculate distance using Google Maps API
def calculate_distance(start_lat, start_lng, end_lat, end_lng):
    gmaps_api_key = 'AIzaSyDlVP8FhcQkBZVS7YqRIdw1Y6Zb6pqfe2U'
    gmaps = googlemaps.Client(key=gmaps_api_key)
    origin = (start_lat, start_lng)
    destination = (end_lat, end_lng)

    # Get distance matrix from Google Maps API
    result = gmaps.distance_matrix(origin, destination)

    try:
        # Get distance matrix from Google Maps API
        result = gmaps.distance_matrix(origin, destination)

        # Check if the result contains the expected data
        if (result and
            'rows' in result and
            result['rows'] and
            'elements' in result['rows'][0] and
            result['rows'][0]['elements'] and
            'distance' in result['rows'][0]['elements'][0] and
            'duration' in result['rows'][0]['elements'][0]):

            distance_km = result['rows'][0]['elements'][0]['distance']['value'] / 1000  # Convert meters to km
            travel_time = result['rows'][0]['elements'][0]['duration']['value'] / 60
            return distance_km, travel_time
        else:
            print(f"Incomplete distance matrix result for origin: {origin}, destination: {destination}")
            return None

    except Exception as e:
        print(f"Error calculating distance for origin: {origin}, destination: {destination}")
        print(f"Error details: {str(e)}")
        return None
    
# Tambahkan kode ini di dalam fungsi utama rekomendasi
def recommend_tourist_destinations(
    user_id, user_lat, user_lng, user_city, user_categories,
    days=None, time=8, budget=None, is_new_user=False,
    departure_city=None, destination_city=None

    ):

    """
    Recommend tourist destinations with sequential distance calculation,
    resetting to original starting point each day.

    Args:
        user_id: The ID of the user for whom the recommendations are being made.
        user_lat (float): Latitude of the user's starting location.
        user_lng (float): Longitude of the user's starting location.
        user_city (str): City preference for the user.
        user_categories: Categories filter (if any).
        days: Number of days for splitting recommendations (if applicable).
        time (float): Fixed daily time limit set to 8 hours.
        budget (float): Budget preference for the user (if applicable).

    Returns:
        A list of recommended destinations for each day, total time used, and total budget spent.
    """

    # Rekomendasi penerbangan
    recommended_flights = pd.DataFrame()  # Default empty DataFrame
    if departure_city and destination_city:
        recommended_flights = filter_flights(departure_city, destination_city, budget)

        # If no flights found within budget, try without budget constraint
        if recommended_flights.empty and budget:
            recommended_flights = filter_flights(departure_city, destination_city)

    # Check if existing user
    user_exists = user_id in rating['User_Id'].unique()

    # Determine categories
    if user_categories is None:
        if user_exists:
            # Get user's past ratings
            user_ratings = rating[rating['User_Id'] == user_id]

            # Merge ratings with place data to get categories
            user_rated_places = pd.merge(rating, merge_data[['Place_Id', 'Category']], on='Place_Id', how='left')

            # Count category frequencies and sort
            category_counts = user_rated_places['Category'].value_counts().reset_index()
            category_counts.columns = ['Category', 'Frequency']

            # Get the most frequent category
            user_categories = category_counts.head(2)['Category'].tolist()
            print(f"User's most frequent category: {user_categories}")

        else:
            city_places = merge_data[merge_data['City'].str.contains(user_city, case=False, na=False)]
            # Group by category and calculate mean rating
            category_ratings = city_places.groupby('Category')['Rating'].mean().sort_values(ascending=False)
            # Select top 3 categories with highest average ratings in the city
            user_categories = category_ratings.head(3).index.tolist()

            print(f"New user - selecting top-rated categories in {user_city}:")
            print(category_ratings)
            print(f"Selected categories: {user_categories}")

    # Ensure category is a list
    if not isinstance(user_categories, list):
        user_categories = [user_categories]

    print(f"Final categories: {user_categories}")

    # Step 1: Filter places based on city and categories (if provided)
    filtered_places = filter_places(
        city=user_city,
        categories=user_categories
        )

    # Step 2: Get Content-Based Filtering recommendations
    cbf_recommendations = calculate_cbf_scores(filtered_places)

    # Step 3: Get Collaborative Filtering recommendations
    place_ids = cbf_recommendations['Place_Id'].unique()

    if user_exists:
        cf_recommendations = predict_ratings(user_id, place_ids)

        # Convert to DataFrame
        cf_recommendations = pd.DataFrame({'Place_Id': place_ids, 'cf_rating': cf_recommendations})

    else:
        # Calculate weighted global average ratings
        global_avg_ratings = merge_data.groupby('Place_Id')['Rating'].mean()

        cf_recommendations = pd.DataFrame({
            'Place_Id': place_ids,
            'cf_rating': [
                global_avg_ratings.get(pid, merge_data['Rating'].mean()) + np.random.uniform(-0.5, 0.5)
                for pid in place_ids
                ]
        })

    # Convert to DataFrame
    cbf_recommendations = pd.DataFrame(cbf_recommendations)

    # Step 4: Combine recommendations
    combined_recommendations = pd.merge(
        cbf_recommendations,
        merge_data[['Place_Id', 'Rating', 'Time_Minutes', 'Price', 'Lat', 'Long']],
        on='Place_Id',
        how='left'
        )
    combined_recommendations = pd.merge(
        combined_recommendations,
        cf_recommendations,
        on='Place_Id',
        how='left'
        )

    # Calculate MSE between CBF and CF recommendations
    combined_recommendations['mse'] = (
        combined_recommendations['Rating'] - combined_recommendations['cf_rating'])**2

    # Sort recommendations by MSE to prioritize consistent recommendations
    combined_recommendations = combined_recommendations.sort_values('mse')

    # Remove duplicates, if any
    combined_recommendations = combined_recommendations.drop_duplicates(subset='Place_Id')

    # Track recommendations per day
    recommendations_per_day = []
    total_time_per_day = []
    total_budget_per_day = []

    # Track visited places across days
    visited_places = set()

    if days:
        for day in range(days):
            day_recommendations = []
            day_total_time = 0
            day_total_budget = 0

            # IMPORTANT: Reset to original starting point for each day
            current_lat = user_lat
            current_lng = user_lng

            # Iterate through sorted recommendations
            for _, place in combined_recommendations.iterrows():
                # Skip if place has been visited in previous days
                if place['Place_Id'] in visited_places:
                    continue

                # Calculate distance from current location to this destination
                distance_km, travel_time = calculate_distance(current_lat, current_lng, place['Lat'], place['Long'])

                # Calculate total time for this place (travel time + visit time in hours)
                place_total_time = (place['Time_Minutes'] / 60) + (travel_time / 60)

                # Check if adding this place would exceed 8-hour limit
                if day_total_time + place_total_time > time:
                    continue

                # Check budget constraint if provided
                if budget and day_total_budget + place['Price'] > budget:
                    continue

                # Add place to daily recommendations
                place_with_distance = place.copy()
                place_with_distance['distance_km'] = distance_km
                place_with_distance['travel_time'] = travel_time
                day_recommendations.append(place_with_distance)

                # Update tracking variables
                day_total_time += place_total_time
                day_total_budget += place['Price']

                # Update current location for next distance calculation
                current_lat = place['Lat']
                current_lng = place['Long']

                # Mark place as visited
                visited_places.add(place['Place_Id'])

            # Convert to DataFrame
            day_recommendations_df = pd.DataFrame(day_recommendations)
            recommendations_per_day.append(day_recommendations_df)
            total_time_per_day.append(day_total_time)
            total_budget_per_day.append(day_total_budget)

    return recommendations_per_day, total_time_per_day, total_budget_per_day, recommended_flights

# Flask route for tourist destination recommendations
@app.route('/recommend_destinations', methods=['POST'])
def get_recommendations():
    data = request.get_json()
    user_id = data.get('user_id')
    user_lat = data.get('user_lat')
    user_lng = data.get('user_lng')
    user_city = data.get('user_city')
    user_categories = data.get('user_categories')
    days = data.get('days')
    time = data.get('time', 8)
    budget = data.get('budget')
    departure_city = data.get('departure_city')
    destination_city = data.get('destination_city')

    if not user_id or not user_lat or not user_lng or not user_city:
        return jsonify({'error': 'Missing required fields'}), 400

    recommendations_per_day, total_time_per_day, total_budget_per_day, recommended_flights = recommend_tourist_destinations(
        user_id, user_lat, user_lng, user_city, user_categories, days, time, budget, departure_city=departure_city, destination_city=destination_city
    )

    return jsonify({
        'recommendations_per_day': [df.to_dict(orient='records') for df in recommendations_per_day],
        'total_time_per_day': total_time_per_day,
        'total_budget_per_day': total_budget_per_day,
        'recommended_flights': recommended_flights.to_dict(orient='records')
    })


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
