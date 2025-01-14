/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.twitter.sdk.android.core.models

import com.google.gson.annotations.SerializedName

/**
 * Represents coordinates of a Tweet's location.
 */
class Coordinates(

    longitude: Double,

    latitude: Double,

    /**
     * The type of data encoded in the coordinates property. This will be "Point" for Tweet
     * coordinates fields.
     */
    @SerializedName("type")
    val type: String
) {

    /**
     * The longitude and latitude of the Tweet's location, as an collection in the form of
     * [longitude, latitude].
     */
    @SerializedName("coordinates")
    val coordinates: List<Double>

    init {
        val coords = ArrayList<Double>(2)
        coords.add(INDEX_LONGITUDE, longitude)
        coords.add(INDEX_LATITUDE, latitude)
        coordinates = ModelUtils.getSafeList(coords)
    }

    val longitude: Double = coordinates[INDEX_LONGITUDE]
    val latitude: Double = coordinates[INDEX_LATITUDE]

    companion object {
        private const val INDEX_LONGITUDE = 0
        private const val INDEX_LATITUDE = 1
    }
}