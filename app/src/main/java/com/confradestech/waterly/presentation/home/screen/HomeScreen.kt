package com.confradestech.waterly.presentation.home.screen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.confradestech.waterly.R
import com.confradestech.waterly.datasources.models.WaterEntry
import com.confradestech.waterly.datasources.states.HomeFragmentState
import com.confradestech.waterly.datasources.states.FaunaMarkerInfoState
import com.confradestech.waterly.datasources.states.FloraMarkerInfoState
import com.confradestech.waterly.datasources.states.ToggleableCheckboxState
import com.confradestech.waterly.datasources.states.WaterMarkerInfoState
import com.confradestech.waterly.utilites.CommonConstants.FIRESTORE_FAUNA_DATA
import com.confradestech.waterly.utilites.CommonConstants.FIRESTORE_FLORA_DATA
import com.confradestech.waterly.utilites.CommonConstants.FIRESTORE_WATERLY_DATA
import com.confradestech.waterly.utilites.CommonConstants.INVALID_LAT_LNG
import com.confradestech.waterly.utilites.CommonConstants.MAPS_ZOOM
import com.confradestech.waterly.utilites.extensions.drawableToBitmap
import com.confradestech.waterly.utilites.extensions.formatFloatToTwoDecimals
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.widgets.ScaleBar
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeState: HomeFragmentState,
    searchEntries: (Float, List<String>) -> Unit,
    openPhoneSettings: () -> Unit,
    openWaterMoreInfo: (WaterEntry?) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()

    val listToSearch = remember {
        mutableListOf<String>()
    }

    val waterEntryToShowMore = remember {
        mutableStateOf<WaterEntry?>(null)
    }

    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val checkBoxList = remember {
        mutableStateListOf<ToggleableCheckboxState>(
            ToggleableCheckboxState(
                isChecked = false,
                toggleableInfo = FIRESTORE_WATERLY_DATA,
            ),
            ToggleableCheckboxState(
                isChecked = false,
                toggleableInfo = FIRESTORE_FLORA_DATA
            ),
            ToggleableCheckboxState(
                isChecked = false,
                toggleableInfo = FIRESTORE_FAUNA_DATA,
            )
        )
    }

    val cameraPositionState = rememberCameraPositionState()

    homeState.userLastLocation?.let {
        coroutineScope.launch {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        MAPS_ZOOM
                    )
                ), 1500
            )
        }
    }

    if (homeState.havePermissions == true) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight()
                        .padding(
                            start = dimensionResource(id = R.dimen.spacing_10),
                            end = dimensionResource(id = R.dimen.spacing_10),
                            top = dimensionResource(id = R.dimen.spacing_10),
                        ),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.onTertiary,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.spacing_10))
                    ) {

                        Slider(
                            modifier = Modifier,
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it })

                        AnimatedVisibility(
                            visible = sliderPosition == 0F,
                            enter = expandVertically(animationSpec = tween(durationMillis = 1000)),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 1000)) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 800
                                )
                            )
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                text = stringResource(id = R.string.home_screen_action_text)
                            )
                        }

                        AnimatedVisibility(
                            visible = sliderPosition * 100 >= 1F,
                            enter = expandVertically(animationSpec = tween(durationMillis = 1000)),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 1000)) + fadeOut(
                                animationSpec = tween(
                                    durationMillis = 800
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(id = R.dimen.spacing_10))
                            ) {
                                Button(
                                    onClick = {
                                        searchEntries(
                                            if (sliderPosition == 1F) {
                                                100F
                                            } else {
                                                sliderPosition.formatFloatToTwoDecimals()
                                            },
                                            listToSearch
                                        )
                                    },
                                    content = {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            text = stringResource(id = R.string.home_screen_action_button).replace(
                                                "#value",
                                                if (sliderPosition == 1F) {
                                                    100.toString()
                                                } else {
                                                    sliderPosition.formatFloatToTwoDecimals()
                                                        .toString()
                                                }
                                            )
                                        )
                                    }
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(dimensionResource(id = R.dimen.spacing_10))
                                ) {
                                    items(checkBoxList.size) { index ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Checkbox(
                                                checked = checkBoxList[index].isChecked,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        listToSearch.add(checkBoxList[index].toggleableInfo.toString())
                                                    } else {
                                                        listToSearch.remove(checkBoxList[index].toggleableInfo.toString())
                                                    }
                                                    checkBoxList[index] =
                                                        checkBoxList[index].copy(isChecked = checked)
                                                }
                                            )
                                            Spacer(
                                                modifier = Modifier.height(
                                                    dimensionResource(
                                                        id = R.dimen.spacing_5
                                                    )
                                                )
                                            )

                                            val text =
                                                when (checkBoxList[index].toggleableInfo.toString()) {
                                                    FIRESTORE_WATERLY_DATA -> stringResource(id = R.string.waterly_data)
                                                    FIRESTORE_FLORA_DATA -> stringResource(id = R.string.flora_data)
                                                    FIRESTORE_FAUNA_DATA -> stringResource(id = R.string.fauna_data)
                                                    else -> ""
                                                }
                                            Text(
                                                text = text,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = waterEntryToShowMore.value != null,
                    enter = expandVertically(animationSpec = tween(durationMillis = 1000)),
                    exit = shrinkVertically(animationSpec = tween(durationMillis = 1000)) + fadeOut(
                        animationSpec = tween(
                            durationMillis = 800
                        )
                    )
                ) {
                    waterEntryToShowMore.value?.let {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(id = R.dimen.spacing_10))
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    openWaterMoreInfo(it)
                                    waterEntryToShowMore.value = null
                                },
                                content = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Info,
                                            contentDescription = ""
                                        )
                                        Text(
                                            stringResource(id = R.string.more_info).replace(
                                                "#name",
                                                it.stationIdentifier
                                            )
                                        )
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_15)))
                    }
                }
            },
            snackbarHost = { /* NO-OP */ },
            floatingActionButton = {/* NO-OP */ },
            floatingActionButtonPosition = FabPosition.End,
            containerColor = MaterialTheme.colorScheme.background,
            content = { _ ->

                val waterPositions = remember {
                    mutableListOf<WaterMarkerInfoState>()
                }
                val faunaPositions = remember {
                    mutableListOf<FaunaMarkerInfoState>()
                }
                val floraPositions = remember {
                    mutableListOf<FloraMarkerInfoState>()
                }

                val localContext = LocalContext.current

                homeState.waterEntryList?.forEach {
                    waterPositions.add(
                        WaterMarkerInfoState(
                            waterEntry = it,
                            markerPosition = LatLng(it.lat, it.lng)
                        )
                    )
                }

                homeState.faunaEntryList?.forEach {
                    faunaPositions.add(
                        FaunaMarkerInfoState(
                            faunaEntry = it,
                            markerPosition = LatLng(it.latitude, it.longitude)
                        )
                    )
                }

                homeState.floraEntryList?.forEach {
                    floraPositions.add(
                        FloraMarkerInfoState(
                            floraEntry = it,
                            markerPosition = LatLng(it.latitude.toDouble(), it.longitude.toDouble())
                        )
                    )
                }

                val mapUiSettings by remember {
                    mutableStateOf(
                        MapUiSettings(mapToolbarEnabled = true, myLocationButtonEnabled = true)
                    )
                }

                Box(Modifier.fillMaxSize()) {

                    AnimatedVisibility(
                        modifier = Modifier.fillMaxSize(),
                        visible = homeState.isLoading != false,
                        enter = EnterTransition.None,
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .wrapContentSize()
                        )
                    }

                    val markerClick: (Marker, WaterEntry?) -> Boolean = { marker, waterEntry ->
                        sliderPosition = 0F
                        waterEntryToShowMore.value = waterEntry
                        if (marker.isInfoWindowShown) {
                            marker.hideInfoWindow()
                        } else {
                            marker.showInfoWindow()
                        }
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(marker.position, MAPS_ZOOM)
                                ), 1500
                            )
                        }
                        false
                    }

                    if (homeState.isLoading == false) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                isBuildingEnabled = true,
                                isIndoorEnabled = true,
                                isMyLocationEnabled = true
                            ),
                            uiSettings = mapUiSettings
                        ) {

                            waterPositions.forEach { markerInfoState ->
                                MarkerInfoWindow(
                                    state = MarkerState(
                                        position = markerInfoState.markerPosition ?: INVALID_LAT_LNG
                                    ),
                                    onClick = { markerClick(it, markerInfoState.waterEntry) },
                                    icon = BitmapDescriptorFactory.fromBitmap(
                                        localContext.drawableToBitmap(R.drawable.baseline_water_drop_24)
                                    )
                                ) {

                                    Surface(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .wrapContentHeight()
                                            .padding(
                                                start = dimensionResource(id = R.dimen.spacing_10),
                                                end = dimensionResource(id = R.dimen.spacing_10),
                                                top = dimensionResource(id = R.dimen.spacing_10),
                                            ),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        tonalElevation = AlertDialogDefaults.TonalElevation
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimensionResource(id = R.dimen.spacing_20))
                                        ) {
                                            markerInfoState.waterEntry?.stationIdentifier?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.station_identifier).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Home,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            markerInfoState.waterEntry?.waterType?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.type).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Build,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            faunaPositions.forEach { faunaMarkerInfoState ->
                                MarkerInfoWindow(
                                    state = MarkerState(
                                        position = faunaMarkerInfoState.markerPosition
                                            ?: INVALID_LAT_LNG
                                    ),
                                    onClick = { markerClick(it, null) },
                                    icon = BitmapDescriptorFactory.fromBitmap(
                                        localContext.drawableToBitmap(R.drawable.baseline_diversity_2_24)
                                    )
                                ) {

                                    Surface(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .wrapContentHeight()
                                            .padding(
                                                start = dimensionResource(id = R.dimen.spacing_10),
                                                end = dimensionResource(id = R.dimen.spacing_10),
                                                top = dimensionResource(id = R.dimen.spacing_10),
                                            ),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        tonalElevation = AlertDialogDefaults.TonalElevation
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimensionResource(id = R.dimen.spacing_20))
                                        ) {
                                            faunaMarkerInfoState.faunaEntry?.scientific_name?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.scientific_name).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.List,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            faunaMarkerInfoState.faunaEntry?.vernacular_name?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.vernacular_name).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Star,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            faunaMarkerInfoState.faunaEntry?.type?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.type).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Build,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            floraPositions.forEach { floraMarkerInfoState ->
                                MarkerInfoWindow(
                                    state = MarkerState(
                                        position = floraMarkerInfoState.markerPosition
                                            ?: INVALID_LAT_LNG
                                    ),
                                    onClick = { markerClick(it, null) },
                                    icon = BitmapDescriptorFactory.fromBitmap(
                                        localContext.drawableToBitmap(R.drawable.baseline_flora_24)
                                    )
                                ) {

                                    Surface(
                                        modifier = Modifier
                                            .wrapContentWidth()
                                            .wrapContentHeight()
                                            .padding(
                                                start = dimensionResource(id = R.dimen.spacing_10),
                                                end = dimensionResource(id = R.dimen.spacing_10),
                                                top = dimensionResource(id = R.dimen.spacing_10),
                                            ),
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        tonalElevation = AlertDialogDefaults.TonalElevation
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(dimensionResource(id = R.dimen.spacing_20))
                                        ) {
                                            floraMarkerInfoState.floraEntry?.scientific_name?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.scientific_name).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.List,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            floraMarkerInfoState.floraEntry?.vernacular_name?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.vernacular_name).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Star,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                            floraMarkerInfoState.floraEntry?.state_province?.let {
                                                if (it.isEmpty().not()) {
                                                    OutlinedTextField(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        value = stringResource(id = R.string.state_province).replace(
                                                            "#name",
                                                            it
                                                        ),
                                                        readOnly = true,
                                                        onValueChange = { /*No-op*/ },
                                                        label = { /*No-op*/ },
                                                        leadingIcon = {
                                                            Icon(
                                                                Icons.Filled.Home,
                                                                contentDescription = ""
                                                            )
                                                        },
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ScaleBar(
                            modifier = Modifier
                                .padding(
                                    bottom = dimensionResource(id = R.dimen.spacing_26),
                                    start = dimensionResource(id = R.dimen.spacing_26)
                                )
                                .align(Alignment.BottomStart),
                            cameraPositionState = cameraPositionState
                        )
                    }
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(
                onClick = { openPhoneSettings() },
                content = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = stringResource(id = R.string.permissions_dialog_permanently_rational_text_body)
                    )
                }
            )
        }
    }
}