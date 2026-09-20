package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.SecCyanPrimary
import com.example.ui.theme.SecEmerald
import com.example.ui.theme.SecRed
import java.io.File

object ProfilePhotoUtils {
    const val PRESET_OFFICER_ONE = "drawable:img_officer_portrait_one"
    const val PRESET_OFFICER_TWO = "drawable:img_officer_portrait_two"

    fun saveBitmapToInternal(context: Context, bitmap: Bitmap): String {
        val filename = "profile_photo_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return file.absolutePath
    }

    fun copyUriToInternal(context: Context, uri: Uri): String {
        return try {
            val filename = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, filename)
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            uri.toString()
        }
    }
}

/**
 * Avatar individual com suporte a foto de perfil obrigatória.
 * Se o perfil não tiver foto, exibe indicação visual clara de pendência.
 */
@Composable
fun ProfilePhotoAvatar(
    photoUri: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showWarningBadge: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    val hasPhoto = !photoUri.isNullOrBlank()

    Box(
        modifier = clickableModifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(2.dp, SecCyanPrimary),
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                when (photoUri) {
                    ProfilePhotoUtils.PRESET_OFFICER_ONE -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_officer_portrait_one),
                            contentDescription = "Foto de perfil de $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ProfilePhotoUtils.PRESET_OFFICER_TWO -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_officer_portrait_two),
                            contentDescription = "Foto de perfil de $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Foto de perfil de $name",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Estado de Foto Ausente (Alerta Obrigatório)
            Surface(
                shape = CircleShape,
                border = BorderStroke(2.dp, if (showWarningBadge) SecRed else MaterialTheme.colorScheme.outline),
                color = if (showWarningBadge) SecRed.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = if (name.isNotBlank()) {
                        name.split(" ")
                            .filter { it.isNotBlank() }
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .take(2)
                            .joinToString("")
                            .uppercase()
                            .ifBlank { "ID" }
                    } else {
                        "ID"
                    }
                    Text(
                        text = initials,
                        fontSize = (size.value * 0.32f).sp,
                        fontWeight = FontWeight.Black,
                        color = if (showWarningBadge) SecRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showWarningBadge) {
                // Badge de alerta de foto obrigatória
                Surface(
                    shape = CircleShape,
                    color = SecRed,
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .size((size.value * 0.38f).coerceAtLeast(14f).dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Foto obrigatória pendente",
                            tint = Color.White,
                            modifier = Modifier.size((size.value * 0.22f).coerceAtLeast(10f).dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente completo de seleção e captura de fotografia obrigatória do perfil.
 */
@Composable
fun ProfilePhotoPickerField(
    currentPhotoUri: String?,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isMandatoryError: Boolean = false
) {
    val context = LocalContext.current

    // Câmera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val path = ProfilePhotoUtils.saveBitmapToInternal(context, bitmap)
            onPhotoSelected(path)
            Toast.makeText(context, "Fotografia capturada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    // Galeria (Zero-Permission Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val path = ProfilePhotoUtils.copyUriToInternal(context, uri)
            onPhotoSelected(path)
            Toast.makeText(context, "Fotografia selecionada da galeria!", Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMandatoryError) SecRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            1.5.dp,
            if (isMandatoryError) SecRed else if (!currentPhotoUri.isNullOrBlank()) SecEmerald.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (!currentPhotoUri.isNullOrBlank()) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (!currentPhotoUri.isNullOrBlank()) SecEmerald else SecRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fotografia do Perfil (OBRIGATÓRIO)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (!currentPhotoUri.isNullOrBlank()) SecEmerald else SecRed
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (!currentPhotoUri.isNullOrBlank()) SecEmerald.copy(alpha = 0.15f) else SecRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (!currentPhotoUri.isNullOrBlank()) "VÁLIDA" else "OBRIGATÓRIA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (!currentPhotoUri.isNullOrBlank()) SecEmerald else SecRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visualização do Avatar Selecionado
            Box(contentAlignment = Alignment.Center) {
                ProfilePhotoAvatar(
                    photoUri = currentPhotoUri,
                    name = "Agente",
                    size = 80.dp,
                    showWarningBadge = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (!currentPhotoUri.isNullOrBlank())
                    "Fotografia de identificação vinculada ao perfil."
                else
                    "Cada agente ou perfil deve ter obrigatoriamente uma fotografia para atestar a identidade nas ações de campo.",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = if (isMandatoryError) SecRed else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Botões de Ação para Tirar / Escolher Foto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecCyanPrimary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tirar Foto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Galeria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Predefinições Oficiais Rápidas (Caso o dispositivo/emulador não tenha câmara disponível)
            Text(
                text = "Ou escolha uma predefinição oficial de identificação:",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Predefinição 1: Oficial Masculino
                val isSelected1 = currentPhotoUri == ProfilePhotoUtils.PRESET_OFFICER_ONE
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        if (isSelected1) 2.dp else 1.dp,
                        if (isSelected1) SecCyanPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    color = if (isSelected1) SecCyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPhotoSelected(ProfilePhotoUtils.PRESET_OFFICER_ONE) }
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_officer_portrait_one),
                            contentDescription = "Retrato Oficial 1",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Retrato 1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Oficial Masc.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Predefinição 2: Oficial Feminino
                val isSelected2 = currentPhotoUri == ProfilePhotoUtils.PRESET_OFFICER_TWO
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        if (isSelected2) 2.dp else 1.dp,
                        if (isSelected2) SecCyanPrimary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    color = if (isSelected2) SecCyanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPhotoSelected(ProfilePhotoUtils.PRESET_OFFICER_TWO) }
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_officer_portrait_two),
                            contentDescription = "Retrato Oficial 2",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Retrato 2", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Oficial Fem.", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo rápido para definir a foto obrigatória do perfil caso esteja sem foto.
 */
@Composable
fun MandatoryPhotoNoticeBanner(
    hasPhoto: Boolean,
    onConfigurePhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasPhoto) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SecRed.copy(alpha = 0.12f)),
            border = BorderStroke(1.5.dp, SecRed.copy(alpha = 0.7f)),
            modifier = modifier
                .fillMaxWidth()
                .clickable { onConfigurePhotoClick() }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SecRed,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Foto Obrigatória Pendente",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = SecRed
                    )
                    Text(
                        text = "Cada perfil deve ter uma foto obrigatória cadastrada para operar no terreno. Toque aqui para adicionar a sua foto agora.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SecRed
                ) {
                    Text(
                        text = "Adicionar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
