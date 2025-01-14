/*
 *  Copyright (C) 2022 Garena Online Pvt Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twitter.sdk.android.tweetcomposer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.internal.UserUtils
import com.twitter.sdk.android.core.models.User
import com.twitter.sdk.android.tweetcomposer.databinding.TwitterComposerViewBinding
import com.twitter.sdk.android.tweetcomposer.internal.util.ObservableScrollView
import java.util.Locale

internal class ComposerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // styled drawables for images
    private var mediaBg: ColorDrawable

    private var callback: ComposerController.ComposerCallbacks? = null

    private val binding: TwitterComposerViewBinding

    private val imageLoader = Twitter.getInstance().getImageLoader()

    init {
        mediaBg =
            ColorDrawable(ContextCompat.getColor(context, R.color.twitter_composer_light_gray))
        binding = TwitterComposerViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        binding.twitterComposerClose.setOnClickListener { callback?.onCloseClick() }
        binding.twitterPostTweet.setOnClickListener { callback?.onTweetPost(getTweetText()) }

        binding.twitterEditTweet.setOnEditorActionListener { _, _, _ ->
            callback?.onTweetPost(getTweetText())
            true
        }
        binding.twitterEditTweet.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                charSequence: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                charSequence: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) = Unit

            override fun afterTextChanged(editable: Editable?) {
                callback?.onTextChanged(getTweetText())
            }
        })

        binding.twitterComposerScrollView.scrollViewListener =
            object : ObservableScrollView.ScrollViewListener {

                override fun onScrollChanged(scrollY: Int) {
                    if (scrollY > 0) {
                        binding.twitterComposerProfileDivider.isVisible = true
                    } else {
                        binding.twitterComposerProfileDivider.isInvisible = true
                    }
                }
            }
    }

    fun setCallbacks(callback: ComposerController.ComposerCallbacks?) {
        this.callback = callback
    }

    /*
     * Sets the profile photo from the User's profile image url or the placeholder background
     * color.
     */
    fun setProfilePhotoView(user: User?) {
        val url = UserUtils.getProfileImageUrlHttps(
            user,
            UserUtils.AvatarSize.REASONABLY_SMALL
        )
        // Passing null url will not trigger any request, but will set the placeholder bg
        imageLoader?.load(url)
            ?.placeholder(mediaBg)
            ?.error(mediaBg)
            ?.into(binding.twitterAuthorAvatar)
    }

    fun getTweetText(): String {
        return binding.twitterEditTweet.text.toString()
    }

    fun setTweetText(text: String) {
        binding.twitterEditTweet.setText(text)
    }

    fun setCharCount(remainingCount: Int) {
        binding.twitterCharCount.text = String.format(Locale.getDefault(), "%d", remainingCount)
    }

    fun setCharCountTextStyle(@StyleRes textStyleResId: Int) {
        TextViewCompat.setTextAppearance(binding.twitterCharCount, textStyleResId)
    }

    fun postTweetEnabled(enabled: Boolean) {
        binding.twitterPostTweet.isEnabled = enabled
    }

    fun setImageView(imageUri: Uri?) {
        imageUri ?: return

        binding.twitterImageView.isVisible = true
        imageLoader?.load(imageUri)
            ?.placeholder(mediaBg)
            ?.error(mediaBg)
            ?.into(binding.twitterImageView)
        binding.twitterVideoView.isVisible = false
    }

    fun setImageView(image: Bitmap?) {
        image ?: return

        binding.twitterImageView.isVisible = true
        binding.twitterImageView.setImageBitmap(image)
        binding.twitterVideoView.isVisible = false
    }

    fun setVideoView(video: Uri?) {
        video ?: return

        with(binding.twitterVideoView) {
            isVisible = true
            setVideoURI(video)
            start()
        }
        binding.twitterImageView.isVisible = false
    }
}