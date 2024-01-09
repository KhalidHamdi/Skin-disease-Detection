# Skin Disease Detection

This GitHub project aims to develop a machine learning model for detecting skin diseases using Python.


# Usage


Since there isn't enough space to upload the libraries and the model file with the project

you will need to do the following steps.

first run the notebook and download the "final_model.h5" file.
you will need the dataset which is in this drive:https://drive.google.com/drive/folders/1-TVy0UcbKeFLRpohzsXDUpCu8rvyUUU9

second put the downloaded model file in model folder.


replace cred and the link for the firebase storage with any firebase storage credentials and link.
you can also comment the lines (28 - 29 - 30 - 31 - 32 - 33 - 34) to try the api without sending the images to the firebase storage.
if you decided to run without firebase storage don't install it.

you will need to install all of the required libraries using the following commands.

pip install keras

pip install Flask

pip install uuid


**don't install this one if you will run without firebase storage**
'pip install firebase_admin'


now the api is ready to be running.

last step is to change the url in the API_connection class in the android application to your local host ip / predict
example : http://192.168.1.9:5000/predict
