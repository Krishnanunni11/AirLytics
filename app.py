from flask import Flask, request, jsonify
import joblib

model = joblib.load("model.pkl")
le_airline = joblib.load("le_airline.pkl")
le_origin = joblib.load("le_origin.pkl")
le_dest = joblib.load("le_dest.pkl")
le_weather = joblib.load("le_weather.pkl")

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json(force=True)

        if not data:
            return jsonify({"error": "No JSON data received"}), 400

        required_fields = ['airline', 'origin', 'destination', 'dep_hour', 'weather']
        missing_fields = [field for field in required_fields if field not in data or data[field] is None]
        if missing_fields:
            return jsonify({"error": f"Missing fields: {', '.join(missing_fields)}"}), 400

        airline_str = str(data['airline']).strip()
        origin_str = str(data['origin']).strip()
        destination_str = str(data['destination']).strip()
        weather_str = str(data['weather']).strip()

        try:
            dep_hour = int(data['dep_hour'])
        except (ValueError, TypeError):
            return jsonify({"error": "dep_hour must be an integer"}), 400

    
        try:
            airline_enc = le_airline.transform([airline_str])[0]
        except ValueError:
            return jsonify({"error": f"Unknown airline: {airline_str}"}), 400

        try:
            origin_enc = le_origin.transform([origin_str])[0]
        except ValueError:
            return jsonify({"error": f"Unknown origin: {origin_str}"}), 400

        try:
            destination_enc = le_dest.transform([destination_str])[0]
        except ValueError:
            return jsonify({"error": f"Unknown destination: {destination_str}"}), 400

        try:
            weather_enc = le_weather.transform([weather_str])[0]
        except ValueError:
            return jsonify({"error": f"Unknown weather: {weather_str}"}), 400

    
        features = [[airline_enc, origin_enc, destination_enc, dep_hour, weather_enc]]
        prediction = model.predict(features)[0]

        return jsonify({"delayed": bool(prediction)})

    except Exception as e:
        return jsonify({"error": f"Internal Server Error: {str(e)}"}), 500

if __name__ == '__main__':
    app.run(debug=True)




