package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ACADEMIC_SESSIONS
import com.example.data.SchoolConfig
import com.example.ui.DashboardMetrics
import com.example.ui.navigation.NavDestination
import com.example.ui.navigation.SidebarCategory
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueContainer
import com.example.ui.theme.SidebarActiveBlue
import com.example.ui.theme.SidebarNavy
import com.example.ui.theme.SidebarNavyBorder
import com.example.ui.theme.SidebarNavyElevated
import com.example.ui.theme.SidebarTextMuted
import com.example.ui.theme.SidebarTextPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletContainer

@Composable
fun SchoolSidebarContent(
    schoolName: String,
    userEmail: String,
    currentDestination: NavDestination,
    onSelectDestination: (NavDestination) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(SidebarNavy)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SidebarNavyElevated)
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AmberGold,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "School Brand Icon",
                        tint = SidebarNavy
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schoolName,
                    style = MaterialTheme.typography.titleSmall,
                    color = SidebarTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = userEmail.ifBlank { "Administrator Portal" },
                    style = MaterialTheme.typography.labelSmall,
                    color = SidebarTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(color = SidebarNavyBorder)

        // Categorized Scrollable Navigation
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            NavDestination.groupedByCategory.forEach { (category, destinations) ->
                Text(
                    text = category.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SidebarTextMuted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 6.dp)
                )

                destinations.forEach { item ->
                    val isSelected = item == currentDestination
                    val isLogout = item == NavDestination.LOGOUT
                    val bgColor = when {
                        isSelected -> SidebarActiveBlue
                        else -> Color.Transparent
                    }
                    val contentColor = when {
                        isLogout -> Color(0xFFFCA5A5)
                        isSelected -> Color.White
                        else -> Color(0xFFCBD5E1)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable {
                                if (isLogout) {
                                    onLogoutClick()
                                } else {
                                    onSelectDestination(item)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("nav_${item.route}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopHeaderAndSessionBar(
    title: String,
    categoryLabel: String,
    selectedSession: String,
    onSessionSelected: (String) -> Unit,
    showMenuButton: Boolean,
    onMenuClick: () -> Unit
) {
    var sessionExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (showMenuButton) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("open_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Sidebar Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Column {
                    Text(
                        text = categoryLabel.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Academic Session Selector (e.g., 2027-28)
            Box {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable { sessionExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("session_selector_chip")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Session: $selectedSession",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Academic Session",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                DropdownMenu(
                    expanded = sessionExpanded,
                    onDismissRequest = { sessionExpanded = false }
                ) {
                    ACADEMIC_SESSIONS.forEach { session ->
                        DropdownMenuItem(
                            text = { Text("Academic Year $session") },
                            onClick = {
                                sessionExpanded = false
                                onSessionSelected(session)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopSummaryMetricsSection(
    metrics: DashboardMetrics,
    isWideScreen: Boolean,
    onCardClick: (NavDestination) -> Unit = {}
) {
    val cards = listOf(
        SummaryCardModel(
            title = "Total Enquiries",
            value = metrics.totalEnquiries.toString(),
            subtitle = "Active leads in session",
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            accentColor = RoyalBlue,
            badgeBg = RoyalBlueContainer,
            target = NavDestination.ADMISSION_ENQUIRY,
            tag = "stat_card_enquiries"
        ),
        SummaryCardModel(
            title = "Form Issued",
            value = metrics.formsIssued.toString(),
            subtitle = "Admission forms sold",
            icon = Icons.Default.Description,
            accentColor = AmberGold,
            badgeBg = AmberGoldContainer,
            target = NavDestination.ADMISSION_ENQUIRY,
            tag = "stat_card_forms_issued"
        ),
        SummaryCardModel(
            title = "Total Admissions",
            value = metrics.totalAdmissions.toString(),
            subtitle = "Enrolled students",
            icon = Icons.Default.School,
            accentColor = EmeraldSuccess,
            badgeBg = EmeraldContainer,
            target = NavDestination.ALL_STUDENTS,
            tag = "stat_card_admissions"
        ),
        SummaryCardModel(
            title = "Form Collection",
            value = "₹${formatAmount(metrics.formCollectionAmount)}",
            subtitle = "Form fee revenue",
            icon = Icons.Default.CurrencyRupee,
            accentColor = VioletAccent,
            badgeBg = VioletContainer,
            target = NavDestination.FEE_COLLECTION,
            tag = "stat_card_form_collection"
        )
    )

    if (isWideScreen) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            cards.forEach { card ->
                MetricStatCard(
                    model = card,
                    onClick = { onCardClick(card.target) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(cards[0], { onCardClick(cards[0].target) }, Modifier.weight(1f))
                MetricStatCard(cards[1], { onCardClick(cards[1].target) }, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(cards[2], { onCardClick(cards[2].target) }, Modifier.weight(1f))
                MetricStatCard(cards[3], { onCardClick(cards[3].target) }, Modifier.weight(1f))
            }
        }
    }
}

private data class SummaryCardModel(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badgeBg: Color,
    val target: NavDestination,
    val tag: String
)

@Composable
private fun MetricStatCard(
    model: SummaryCardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag(model.tag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = model.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = model.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = model.badgeBg,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = model.icon,
                        contentDescription = model.title,
                        tint = model.accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SchoolFooterContactBar(
    config: SchoolConfig
) {
    val context = LocalContext.current

    fun openExternalLink(url: String) {
        try {
            val safeUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)))
        } catch (_: Exception) {
        }
    }

    fun dialHelpline(number: String) {
        try {
            val digits = number.filter { it.isDigit() || it == '+' }
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
        } catch (_: Exception) {
        }
    }

    Surface(
        color = SidebarNavy,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("school_footer_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Helpline Contact Info
                Row(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SidebarNavyElevated)
                        .clickable { dialHelpline(config.helplineNumber) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("footer_helpline_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Helpline",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Helpline: ${config.helplineNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SidebarTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Branded Quick Social / Platform Links (Website, YouTube x2, Facebook, Instagram)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialBrandPill(
                        label = "Website",
                        icon = Icons.Default.Language,
                        brandColor = Color(0xFF0EA5E9),
                        tag = "footer_website_link",
                        onClick = { openExternalLink(config.websiteUrl) }
                    )
                    SocialBrandPill(
                        label = "YouTube Main",
                        icon = Icons.Default.SmartDisplay,
                        brandColor = Color(0xFFEF4444),
                        tag = "footer_youtube_main_link",
                        onClick = { openExternalLink(config.youtubeMainUrl) }
                    )
                    SocialBrandPill(
                        label = "YT Kids/Jr",
                        icon = Icons.Default.PlayCircleFilled,
                        brandColor = Color(0xFFDC2626),
                        tag = "footer_youtube_jr_link",
                        onClick = { openExternalLink(config.youtubeJuniorUrl) }
                    )
                    SocialBrandPill(
                        label = "Facebook",
                        icon = Icons.Default.Public,
                        brandColor = Color(0xFF1877F2),
                        tag = "footer_facebook_link",
                        onClick = { openExternalLink(config.facebookUrl) }
                    )
                    SocialBrandPill(
                        label = "Instagram",
                        icon = Icons.Default.CameraAlt,
                        brandColor = Color(0xFFE1306C),
                        tag = "footer_instagram_link",
                        onClick = { openExternalLink(config.instagramUrl) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialBrandPill(
    label: String,
    icon: ImageVector,
    brandColor: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = brandColor.copy(alpha = 0.18f),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, brandColor.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(brandColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatAmount(amount: Long): String {
    return String.format("%,d", amount)
}
