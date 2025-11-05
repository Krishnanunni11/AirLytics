import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
import matplotlib.pyplot as plt
import joblib

df = pd.read_csv("sample_flights.csv", dtype=str)

df['dep_hour'] = pd.to_numeric(df['dep_hour'], errors='coerce').fillna(0).astype(int)
df['delay_minutes'] = pd.to_numeric(df['delay_minutes'], errors='coerce').fillna(0)

df['airline'] = df['airline'].fillna('UNKNOWN').astype(str)
df['origin'] = df['origin'].fillna('UNKNOWN').astype(str)
df['destination'] = df['destination'].fillna('UNKNOWN').astype(str)
df['weather'] = df['weather'].fillna('CLEAR').astype(str)

df['delayed'] = df['delay_minutes'].apply(lambda x: 1 if x > 15 else 0)

le_airline = LabelEncoder()
le_origin = LabelEncoder()
le_dest = LabelEncoder()
le_weather = LabelEncoder()

df['airline_enc'] = le_airline.fit_transform(df['airline'])
df['origin_enc'] = le_origin.fit_transform(df['origin'])
df['dest_enc'] = le_dest.fit_transform(df['destination'])
df['weather_enc'] = le_weather.fit_transform(df['weather'])

X = df[['airline_enc', 'origin_enc', 'dest_enc', 'dep_hour', 'weather_enc']]
y = df['delayed']

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

y_pred = model.predict(X_test)
print("Classification Report:\n")
print(classification_report(y_test, y_pred))

feature_names = X.columns
importances = model.feature_importances_
plt.figure(figsize=(8, 4))
plt.barh(feature_names, importances, color='skyblue')
plt.xlabel("Feature Importance")
plt.title("What Influences Flight Delays?")
plt.tight_layout()
plt.show()

joblib.dump(model, "model.pkl")
joblib.dump(le_airline, "le_airline.pkl")
joblib.dump(le_origin, "le_origin.pkl")
joblib.dump(le_dest, "le_dest.pkl")
joblib.dump(le_weather, "le_weather.pkl")

print("Model and encoders saved successfully.")









