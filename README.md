# SuperCarApp

### Technologies :rocket:
Written in Kotlin, based on latest recommendations by Google.
* MVVM architecture
* Room database - for cached external data
* Retrofit /moshi - for network data 
* LiveData - for responsive UI changes
* Coroutines  - for async tasks
* DataBinding
* Hilt
* Tests (only database tested)
* CoLocation (RxLocation is deprecated)
* EasyPermissions (permission handling)

### Features v.1
* Tracking status of api request
* Store API data in DB if succesful call, sync data every 5 seconds
* Error handling if network error
* Data showed in RecyclerView
* Filter by Plate number (SearchView)
* Filter by battery left (Slider)
* Dependency injection (Hilt)

<img width="150" src="https://user-images.githubusercontent.com/52376789/95460879-2cf95880-097e-11eb-814f-151bcd178286.png"> <img width="150" src="https://user-images.githubusercontent.com/52376789/95460834-1eab3c80-097e-11eb-8d49-59404b6b32ad.png">

### Features v.2
* Permissions handling
* Location tracking
* Distance user-to-car calculation and live updates
* Sorting by distance
* Orientation change data and View resets fixed
* Using Mediator Live data for sort and filters - as single-source-data

<img width="150" src="https://user-images.githubusercontent.com/52376789/96118851-c8e21180-0ef4-11eb-8f1a-2629bbeac72d.png"> <img width="150" src="https://user-images.githubusercontent.com/52376789/96118853-ca133e80-0ef4-11eb-95ab-0aa16c780d0f.png">

### Features v.3
* Permission handling (switched to RxPermissions)
* Improved filtering (RxKotlin)
* Improved formatting (ktlint.gradle)
