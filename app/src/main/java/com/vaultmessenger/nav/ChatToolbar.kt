package com.vaultmessenger.nav

/*
 * Copyright 2024 Your Name
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * This file incorporates work from the following project:
 * Original Project: Material Symbols for Jetpack Compose
 * Original Author: @alexstyl
 * URL: https://www.composables.com/icons
 * Changes: no changes made and added as is
 */


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.vaultmessenger.ProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbar(
    navController: NavHostController,
    receiverUID: String,
    profilePhoto: String) {
    TopAppBar(
        title = {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                ProfileImage(
                    userPhotoUrl = profilePhoto,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .border(1.dp, color = Color.Blue, shape = CircleShape)
                        //.padding(5.dp),
                    )
                Spacer(modifier = Modifier.padding(horizontal = 3.dp))
                Text(
                    text = receiverUID,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                   style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = {navController.navigateUp()}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "menu items"
                )
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = rememberVideocam(),
                    contentDescription = "video call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "phone call",
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "more options",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0D62CA),
            scrolledContainerColor = Color.Black,
            actionIconContentColor = Color.White,
            navigationIconContentColor = Color(0xffb3cbff)
        ),
    )
}

@Composable
fun rememberVideocam(): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "videocam",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(6.292f, 33.083f)
                quadToRelative(-1.042f, 0f, -1.834f, -0.791f)
                quadToRelative(-0.791f, -0.792f, -0.791f, -1.834f)
                verticalLineTo(9.542f)
                quadToRelative(0f, -1.042f, 0.791f, -1.834f)
                quadToRelative(0.792f, -0.791f, 1.834f, -0.791f)
                horizontalLineToRelative(20.916f)
                quadToRelative(1.042f, 0f, 1.834f, 0.791f)
                quadToRelative(0.791f, 0.792f, 0.791f, 1.834f)
                verticalLineToRelative(8.5f)
                lineToRelative(5.375f, -5.375f)
                quadToRelative(0.292f, -0.292f, 0.73f, -0.146f)
                quadToRelative(0.437f, 0.146f, 0.437f, 0.604f)
                verticalLineToRelative(13.75f)
                quadToRelative(0f, 0.458f, -0.437f, 0.604f)
                quadToRelative(-0.438f, 0.146f, -0.73f, -0.187f)
                lineToRelative(-5.375f, -5.334f)
                verticalLineToRelative(8.5f)
                quadToRelative(0f, 1.042f, -0.791f, 1.834f)
                quadToRelative(-0.792f, 0.791f, -1.834f, 0.791f)
                close()
                moveToRelative(0f, -2.625f)
                horizontalLineToRelative(20.916f)
                verticalLineTo(9.542f)
                horizontalLineTo(6.292f)
                verticalLineToRelative(20.916f)
                close()
                moveToRelative(0f, 0f)
                verticalLineTo(9.542f)
                verticalLineToRelative(20.916f)
                close()
            }
        }.build()
    }
}