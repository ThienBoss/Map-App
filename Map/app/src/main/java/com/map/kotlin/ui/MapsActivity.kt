package com.map.kotlin.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.map.kotlin.adapter.BookmarkInfoWindowAdapter
import com.map.kotlin.R
import com.map.kotlin.adapter.BookmarkListAdapter
import com.map.kotlin.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import kotlinx.android.synthetic.main.activity_bookmark_details.toolbar
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_map.*
import kotlinx.android.synthetic.main.main_view_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placeClient: PlacesClient
    private lateinit var mapViewModel: MapsViewModel
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    companion object {
        const val EXTRA_BOOKMARK_ID = "com.map.kotlin.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity1"
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
    }

    private var markers = HashMap<Long, Marker>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setLocationClient()
        setupPlaceClient()
        setupToolbar()
        setupNavigationDrawer()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListener()
        setupViewModel()
        getCurrentLocation()
    }

    private fun setLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )

    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission()
        } else {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val updateCamera = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    map.moveCamera(updateCamera)
                } else {
                    Log.e(TAG, "No location found ! ")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location Permission Denied !")
            }
        }
    }

    private fun setupPlaceClient() {
        Places.initialize(applicationContext, "AIzaSyD1mu3Lq0fmFTXzLkny42cJcWTFdmfwCt0")
        placeClient = Places.createClient(this)
    }

    private fun displayPoi(poi: PointOfInterest) {
        displayPoiGetPlaceStep(poi)

    }

    private fun displayPoiGetPlaceStep(poi: PointOfInterest) {
        val placeId = poi.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.LAT_LNG,
            Place.Field.PHOTO_METADATAS
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placeClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            displayPoiGetPhotoStep(place)
        }
            .addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    Log.e(
                        TAG,
                        "Place not found " + " ," + exception.message + "Status Code" + statusCode
                    )
                }
            }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadta = place
            .photoMetadatas?.get(0)
        if (photoMetadta == null) {
            displayPoiDisplayStep(place, null)
            return
        } else {
            val photoRequest = FetchPhotoRequest
                .builder(photoMetadta)
                .setMaxHeight(
                    resources.getDimensionPixelSize(
                        R.dimen.default_image_height
                    )
                )
                .setMaxWidth(
                    resources.getDimensionPixelSize(
                        R.dimen.default_image_width
                    )
                )
                .build()

            placeClient.fetchPhoto(photoRequest)
                .addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.bitmap
                    displayPoiDisplayStep(place, bitmap)
                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Log.e(
                            TAG,
                            "Place not found " + " ," + exception.message + "Status Code" + statusCode
                        )
                    }
                }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        if (photo == null) {
            BitmapDescriptorFactory
                .defaultMarker()
        } else {
            BitmapDescriptorFactory.fromBitmap(photo)
        }
        val maker = map.addMarker(
            MarkerOptions()
                .position(place.latLng as LatLng)
                .title(place.name)
                .snippet(place.address)
        )
        maker?.tag = PlaceInfo(place, photo)
        maker?.showInfoWindow()
    }

    private fun setupViewModel() {
        mapViewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)
        createBookmarkBookmarkViewObserver()
    }

    private fun setupMapListener() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener {
            handlerInfoWindowClick(it)
        }
        fab.setOnClickListener {
            searchAtCurrentLocation()
        }
        map.setOnMapLongClickListener {
            newBookmark(it)
        }
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    private fun handlerInfoWindowClick(maker: Marker) {
        when (maker.tag) {
            is PlaceInfo -> {
                val placeInfo = (maker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapViewModel.addBookmarkFromPlace(
                            placeInfo.place,
                            placeInfo.image
                        )
                    }
                }
                maker.remove()
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkView = (maker.tag as MapsViewModel.BookmarkView)
                maker.hideInfoWindow()
                bookmarkView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkView): Marker? {
        val marker = map.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(bookmark.categoryResourceId?.let { BitmapDescriptorFactory.fromResource(it) })
                .alpha(0.8f)
        )
        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }
        return marker
    }

    private fun displayAllBookmarks(bookmarks: List<MapsViewModel.BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkBookmarkViewObserver() {
        mapViewModel.getBookmarkViews()
            ?.observe(this, androidx.lifecycle.Observer<List<MapsViewModel.BookmarkView>> {
                map.clear()
                markers.clear()
                it?.let {
                    displayAllBookmarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }

    private fun setupNavigationDrawer() {
        val layoutManager = LinearLayoutManager(this)
        bookmarkRecylerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        bookmarkRecylerView.adapter = bookmarkListAdapter
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        drawerLayout.closeDrawer(drawerView)
        val marker = markers[bookmark.id]
        marker?.showInfoWindow()
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchAtCurrentLocation() {
        val placeFields = listOf<Place.Field>(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES
        )

        val bound = RectangularBounds.newInstance(map.projection.visibleRegion.latLngBounds)
        try {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
                .setLocationBias(bound).build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {
        } catch (e: GooglePlayServicesNotAvailableException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapToLocation(location)
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    private fun newBookmark(latlng: LatLng) {
        GlobalScope.launch{
            val bookmarkId = mapViewModel.addBookmark(latlng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }




}
