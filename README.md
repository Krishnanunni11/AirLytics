# ✈️ AirLytics

**AirLytics** is a hybrid project that integrates **Java (for the User Interface)** and **Python (for the Predictive Model)** to forecast whether a flight will be **“DELAYED”** or **“ON TIME.”**  
This project demonstrates how **Object-Oriented Programming (OOP)** concepts can merge with **Data Science** techniques to create an intelligent, real-world analytics system.  

---

## 🧠 Project Overview  

When our OOP (Java) project was assigned, I wanted to create something that reflects my interest in **data analytics** and **machine learning**.  
So I built **AirLytics**, a micro project that combines my passion for **data-driven prediction systems** with **Java programming**.  

AirLytics uses a **Random Forest Classifier** trained in Python and integrates it with a **Java-based front-end UI**.  
Users can upload or input a custom **Flight Chart (CSV file)**, and the system will predict the flight’s status.  

> 📝 **Note:**  
> This prediction is **purely based on the flight chart I created** for this project — not real-world airline data.  
> If you create a similar flight chart with the same column and row labels, **AirLytics** will predict perfectly based on your data.

---

## ⚙️ Tech Stack  

| Component | Technology Used |
|------------|----------------|
| **Frontend (UI)** | Java (Swing / AWT) |
| **Backend (ML Model)** | Python (Flask) |
| **Algorithm** | Random Forest Classifier |
| **Data Handling** | CSVUtils.java for CSV parsing |
| **Integration** | HTTP communication between Java and Flask |
| **Dataset** | Custom Flight Dataset (CSV format) |

---

## 🔁 Workflow  

1. **Java UI (Frontend)**  
   - User opens the AirLytics application.  
   - Uploads or inputs flight data (CSV).  
   - The app parses and structures the data using **CSVUtils.java**.  

2. **Data Transmission**  
   - Java sends the flight information to the **Python Flask API** (`/predict`) as JSON.  

3. **Python Backend (Model Prediction)**  
   - The trained **Random Forest model** loads (`model.pkl`).  
   - Data is processed, and predictions are generated (“Delayed” or “On Time”).  

4. **Result Display**  
   - Flask returns the prediction to the Java frontend.  
   - The Java UI displays the result in a clear, user-friendly format.  

---

## 🗂️ Project Structure  
Kindly note that: After loading the files you have to organize it based on the given File Structure

AirLytics/
├── UI/
│ ├── CSVUtils.java
│ ├── FlightPredictor.java
│ ├── json-20231013 
├── Model/
│ ├── app.py # Flask server
│ ├── model.pkl # Trained Random Forest Model
│ ├── train_model.py
├── Dataset/
│ └── flight_data.csv
│
└── README.md


---

## 🧩 Flowchart  
<img width="2048" height="1811" alt="flow" src="https://github.com/user-attachments/assets/2766b678-5aa9-45f7-85d7-c5bc9d4c3db0" />


---

## 🧰 How to Run  

### 🖥️ Step 1 – Run Flask Server (Python)
```
cd Model
pip install flask joblib
python app.py
```
<img width="1920" height="1020" alt="Screenshot 2025-09-25 073320" src="https://github.com/user-attachments/assets/cdfb6351-c75b-4c84-908f-9cc334aae800" />
On a new cmd
```
cd UI
javac -cp .;json-20231013.jar FlightPredictor.java CSVUtils.java
java -cp .;json-20231013.jar FlightPredictor
```
---

## 🧾 Step 3 – Upload Flight Chart

Upload your custom CSV file with appropriate columns.

Click Predict to see the model output.

### Sample CSV
flight_no,airline,origin,destination,dep_hour,weather,delay_minutes
6E221,IndiGo,DEL,BLR,20,RAIN,10
US635,US Airways,SMF,PHX,6,SNOW,12
US646,US Airways,OAK,PHX,6,SUNNY,8
US1941,US Airways,PVD,CLT,6,CLEAR,-14
UA422,SpiceJet,RNO,DEN,6,SNOW,14

## Screen Shorts
<img width="1266" height="700" alt="Screenshot 2025-09-23 223400" src="https://github.com/user-attachments/assets/f4d75b42-2a7a-4137-819a-4a2993a26572" />
<img width="1204" height="685" alt="Screenshot 2025-09-23 223437" src="https://github.com/user-attachments/assets/34eb3b18-d855-4053-9766-7733d96bc91a" />
<img width="1080" height="691" alt="Screenshot 2025-09-23 223509" src="https://github.com/user-attachments/assets/4c522963-aa18-48e6-959b-35184bc853e4" />

---
## 💡 Key Learnings

Integration of OOP (Java) with Machine Learning (Python)

Building RESTful communication between two languages

Understanding ensemble methods like Random Forest

Designing predictive systems using structured CSV data

---
## 🏁 Conclusion

AirLytics proves that OOP principles and AI can seamlessly come together to solve practical problems.
By merging Java’s robust interface capabilities with Python’s predictive intelligence, this project highlights the future of cross-language, data-driven systems.

---

## 🧾 Disclaimer

This prediction system is based purely on a synthetic flight dataset created for learning purposes.
The model is not trained on real aviation data and should not be used for real-world decision making.

---

## 👨‍💻 Author

Krishnanunni H Pillai, 
Aspiring Data Scientist & Software Developer,
Passionate about Machine Learning, Data Analytics, and Intelligent Systems.





