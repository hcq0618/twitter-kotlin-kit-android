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
package com.twitter.sdk.android.core

import android.content.Context

/**
 * Configurable Twitter options
 */
class TwitterConfig private constructor(
    val context: Context,
    val logger: Logger?,
    val twitterAuthConfig: TwitterAuthConfig?,
    val imageLoader: TwitterImageLoader?,
    val debug: Boolean
) {
    /**
     * Builder for creating [TwitterConfig] instances.
     */
    class Builder(context: Context) {

        private val context = context.applicationContext
        private var logger: Logger? = null
        private var twitterAuthConfig: TwitterAuthConfig? = null
        private var debug: Boolean = false
        private var imageLoader: TwitterImageLoader? = null

        /**
         * Sets the [Logger] to build with.
         */
        fun logger(logger: Logger) = apply {
            this.logger = logger
        }

        /**
         * Sets the [TwitterAuthConfig] to build with.
         */
        fun twitterAuthConfig(authConfig: TwitterAuthConfig) = apply {
            twitterAuthConfig = authConfig
        }

        /**
         * Enable debug mode
         */
        fun debug(debug: Boolean) = apply {
            this.debug = debug
        }

        fun imageLoader(imageLoader: TwitterImageLoader) = apply {
            this.imageLoader = imageLoader
        }

        /**
         * Build the [TwitterConfig] instance
         */
        fun build(): TwitterConfig {
            return TwitterConfig(
                context,
                logger,
                twitterAuthConfig,
                imageLoader,
                debug
            )
        }
    }
}