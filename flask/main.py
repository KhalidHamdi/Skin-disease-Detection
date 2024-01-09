import flask
import firebase_admin
from firebase_admin import credentials, storage
import keras
from keras_preprocessing import image
import numpy as np
import uuid

# Create flask application
app = flask.Flask(__name__)

#Firebase connecting 
cred = credentials.Certificate(r"D:\mygraduationproject-68da1-firebase-adminsdk-87usb-58183a4129.json")
firebase_admin.initialize_app(cred, {'storageBucket': 'mygraduationproject-68da1.appspot.com'})  

@app.route("/prediction" , methods = ["GET" , "Post"])
def prediction():
    # Get the uploaded image
    imgFile = flask.request.files['image']

    #Generate a unique image name 
    unique_suffix = str(uuid.uuid4())
    imageName = imgFile.filename + unique_suffix + '.jpg'
    imgFile.save(imageName)
    print("Image name:" + imageName)

    # Upload the image to the storage bucket firebase
    storage_bucket = storage.bucket()
    blob = storage_bucket.blob(imageName)
    try:
        blob.upload_from_filename(imageName)
        print(f"File '{imageName}' successfully uploaded to the storage bucket.")
    except Exception as e:
        print(f"Error uploading '{imageName}' to the storage bucket: {str(e)}")


    #Load Model
    model_path = r"D:\model\final_model.h5"
    loaded_model = keras.models.load_model(model_path)

    #Preprocess image
    img = image.load_img(imageName,target_size=(224,224))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    images = np.vstack([x])


    # Make predictions 
    prediction_result = loaded_model.predict(images).argmax(axis=-1)

    skin_conditions = {
    0: "Actinic keratoses and intraepithelial carcinoma",
    1: "Benign lesions of the keratosis",
    2: "Dermatofibroma",
    3: "Melanoma",
    4: "Normal Skin",
    5: "Vascular lesions"
    }

    result_label = skin_conditions.get(prediction_result[0])

    return result_label

if __name__ == "__main__":
    app.run(host="0.0.0.0" , debug= True)