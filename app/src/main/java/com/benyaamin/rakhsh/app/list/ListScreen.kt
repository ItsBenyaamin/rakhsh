package com.benyaamin.rakhsh.app.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.benyaamin.rakhsh.DownloadStatus
import com.benyaamin.rakhsh.app.R
import com.benyaamin.rakhsh.app.ui.components.CustomIconButton
import com.benyaamin.rakhsh.model.Download
import com.benyaamin.rakhsh.model.DownloadProgress
import kotlinx.coroutines.flow.flowOf

@Composable
fun ListScreen(list: List<Download>, actionCallback: (ListActions) -> Unit) {
    var shouldShowAddDownloadDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        Column {
            TopBar()

            LazyColumn {
                items(items = list, key = { it.id }) {
                    DownloadItem(it, actionCallback)
                }
            }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            content = {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            },
            onClick = {
                shouldShowAddDownloadDialog = true
            }
        )

        if (shouldShowAddDownloadDialog) {
            AddDownloadDialog(
                save = { url ->
                    actionCallback(ListActions.EnqueueNewDownload(url))
                    shouldShowAddDownloadDialog = false
                }, {
                    shouldShowAddDownloadDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .height(60.dp)
            .shadow(elevation = 2.dp),
        title = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        },
    )
}

@Composable
fun DownloadItem(item: Download, actionCallback: (ListActions) -> Unit) {
    val progress = item.progressFlow.collectAsStateWithLifecycle(null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.fileName,
                maxLines = 1,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val statusColor = when (item.status) {
                    DownloadStatus.Completed -> Color.Green
                    DownloadStatus.Downloading -> Color.Magenta
                    DownloadStatus.Error -> Color.Red
                    else -> Color.Black
                }

                val statusText =
                    if (item.status == DownloadStatus.Error) item.error ?: item.status.name
                    else item.status.name

                Text(text = "Status: ")
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.status == DownloadStatus.Downloading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    progress = { progress.value?.progress?.div(100f) ?: 0f },
                    gapSize = 0.dp,
                    strokeCap = StrokeCap.Round,
                    color = Color.Blue,
                    trackColor = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (item.status != DownloadStatus.Completed) {
                    if (item.status == DownloadStatus.Downloading) {
                        CustomIconButton(R.drawable.baseline_stop_24) {
                            actionCallback(ListActions.StopDownload(item.id))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        CustomIconButton(R.drawable.baseline_pause_24) {
                            actionCallback(ListActions.PauseDownload(item.id))
                        }
                    } else {
                        CustomIconButton(R.drawable.baseline_play_arrow_24) {
                            actionCallback(ListActions.StartDownload(item.id, item.status))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                }

                CustomIconButton(R.drawable.baseline_delete_24) {
                    actionCallback(ListActions.RemoveDownload(item.id))
                }
            }
        }
    }
}

@Composable
fun AddDownloadDialog(save: (String) -> Unit, dismissed: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var emptyFieldsError by remember { mutableStateOf(false) }

    Dialog({ dismissed() }) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add new download",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }

                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = "Url:"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = url,
                    onValueChange = { url = it },
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (emptyFieldsError) {
                    Text(
                        text = "Please enter a url!",
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        if (url.isNotEmpty()) save(url)
                        else emptyFieldsError = true
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Save")
                }

                TextButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        dismissed()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "Cancel")
                }

            }
        }
    }
}

@Preview
@Composable
fun PreviewListScreen() {
    ListScreen(emptyList()) { }
}

@Preview
@Composable
fun PreviewDownloadItem() {
    DownloadItem(
        Download(
            id = 0,
            fileName = "Download item 1 with long name.pdf",
            status = DownloadStatus.Completed,
            error = "Network error",
            flowOf(
                DownloadProgress(
                    0,
                    null,
                    0L,
                    0L,
                    100
                )
            )
        )
    ) {}
}

@Preview
@Composable
fun PreviewAddDownloadDialog() {
    AddDownloadDialog({}, {})
}