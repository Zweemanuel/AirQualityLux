# Air Quality Lux Application

Air Quality Lux is an Android application that provides real-time air quality information in Luxembourg by using the [Air Quality Sensor Network for Luxembourg](https://data.public.lu/en/datasets/air-quality-sensor-network-for-luxembourg/)
.

## Overview

This application uses [OpenStreetMap](https://www.openstreetmap.org/) for its map visualization, along with a set of markers that are color coded with regards to air quality. Each marker represents an air quality sensor located somewhere in Luxembourg.

## Core Features

The application provides several key features:

1. **Map View**: Users can view a detailed map of Luxembourg, that is customized using the [Geoapify Api](https://www.geoapify.com/map-tiles/).

2. **Map navigation**: In addition to the default navigation methods of a Map View, users can use a special seekbar to zoom in and out using 1 finger from any point, a center button to reset the view, and a focus mode which restricts the Map View to Luxembourg.

3. **User Location**: The app provides the ability to track and display the user's current location, using the built-in GPS capabilities of the user's device.

4. **Data processing**: The [Data](https://data.sensor.community/airrohr/v1/filter/country=LU).  gathered from the Sensor Network is quite messy, thus in the `FetchDataTask` the data had to be processed to only include valid data, unique sensor locations, the latest data only, and filter the needed data values to create a description. This data is then held in a `DataHolder` singleton to be used accross different components in the application.

5. **Real-time Air Quality Data**: The map includes color coded markers for real-time air quality data. These markers can be clicked on to open a description using the `CustomWindowAdapter` with the exact measurements of the sensor and a timestamp of when that information was last updated.

6. **Data Refresh**:The app periodically refreshes the air quality data (every 60 minutes) to ensure users have the most up-to-date information.

7. **Menu**: A bottom sheet with specific behaviour lets users swipe up/down to show/hide these features:
    - **Marker count**: A display of how many sensors are currently displayed on the map using markers.
    - **Information Btn**: An `InfoActivity` with information about the data values and their meaning.
    - **Marker List Btn**: A `MarkerListActivity` that displays all markers as items in a list. A search bar is implemented in this activity to allow the users to better identify their points of interest in the network of sensors.
    - **Customization**: Checkboxes and switches that allow the user to experience the application the way they want. These checkboxes/switches include the ability to hide/show the markers including the sensor details, the focus switch mentioned in "Map navigation", and two checkboxes to hide/show the center button and special seekbar.

 

6. **User Preferences**: Users can customize their viewing preferences using the checkboxes and switches in the Bottom sheet Menu, these settings are preserved across sessions using SharedPreferences.

## Future development

This application was built in such a way that it can be extended easily to include more features:
- **Cover more countries**: Make more/different requests to [data.sensor.community](https://data.sensor.community/airrohr/v1/filter/country=LU), which covers multiple countries. (design component to manage which ones are shown)
- **Proximity Notifications**: A service that constantly monitors distance between sensor and user locations.
- **Favorites alert**: A service that checks the favorite sensor location and alerts the user once it reaches a threshold.
- **Data quality History**: Storing the sensor data externally and providing it to the application.

## Getting Started

To get started with the Air Quality Lux application, simply clone this repository and open it in Android Studio. From there, you can run the app on an emulator or a connected Android device.
