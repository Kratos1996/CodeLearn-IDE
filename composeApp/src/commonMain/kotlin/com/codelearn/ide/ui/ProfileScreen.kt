package com.codelearn.ide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.codelearn.ide.auth.AuthState
import com.codelearn.ide.auth.AuthViewModel
import com.codelearn.ide.course.CourseRepository
import com.codelearn.ide.course.CourseViewModel
import com.codelearn.ide.course.XPRewards

@Composable
fun ProfileScreenContent(
    authVm: AuthViewModel,
    courseVm: CourseViewModel,
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSignIn: () -> Unit = {}
) {
    val authState by authVm.authState.collectAsState()
    val allProgress by courseVm.allProgress.collectAsState()

    val user = (authState as? AuthState.LoggedIn)?.user

    Column(modifier = Modifier.fillMaxSize().background(IDEColors.bg0)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(IDEColors.bg1)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("←", fontSize = 20.sp, color = IDEColors.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = 12.dp))
            Text("Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = IDEColors.textPrimary, modifier = Modifier.weight(1f))
            TextButton(onClick = onSignOut) {
                Text("Sign Out", color = IDEColors.red, fontSize = 13.sp)
            }
        }

        if (user == null) {
            // Guest — show sign-in prompt
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(IDEColors.bg2),
                        contentAlignment = Alignment.Center
                    ) { Text("👤", fontSize = 48.sp) }

                    Text("You're a Guest",
                        color = IDEColors.textPrimary,
                        fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    Text(
                        "Sign in to save your progress, earn badges and certificates, and sync across devices.",
                                color = IDEColors.textSecondary, fontSize = 14.sp,
                        textAlign = TextAlign.Center, lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = onSignIn,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IDEColors.accent)
                    ) {
                        Text("Sign In / Create Account",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // Show local progress even as guest
                    val allProgress by courseVm.allProgress.collectAsState()
                    if (allProgress.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Your local progress (not synced):",
                            color = IDEColors.textMuted, fontSize = 12.sp)
                        allProgress.entries
                            .filter { it.value.completedStageIds.isNotEmpty() }
                            .take(3)
                            .forEach { (courseId, prog) ->
                                Text("• $courseId: ${prog.completedStageIds.size} stages done",
                                    color = IDEColors.textSecondary, fontSize = 12.sp)
                            }
                    }
                }
            }
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Avatar + name ────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(listOf(
                                IDEColors.accent.copy(alpha = 0.2f), IDEColors.bg2
                            ))
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Avatar circle
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape)
                                .background(IDEColors.accent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.avatarInitials, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                                color = Color.White)
                        }
                        Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                            color = IDEColors.textPrimary)
                        Text(user.email, fontSize = 13.sp, color = IDEColors.textSecondary)
                        // Level badge
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(IDEColors.accent.copy(alpha = 0.2f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Level ${user.level} • ${user.xp} XP",
                                color = IDEColors.accent, fontSize = 14.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        // XP progress to next level
                        val nextLevelXP = XPRewards.xpForNextLevel(user.xp)
                        val prevLevelXP = if (user.level == 1) 0 else when(user.level){
                            2->100;3->250;4->500;5->1000;6->2000;7->3500;8->5000;9->7000;else->0
                        }
                        val progress = if (nextLevelXP == Int.MAX_VALUE) 1f
                        else (user.xp - prevLevelXP).toFloat() / (nextLevelXP - prevLevelXP)
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${user.xp} XP", color = IDEColors.textMuted, fontSize = 11.sp)
                                Text(if (nextLevelXP == Int.MAX_VALUE) "Max Level"
                                else "$nextLevelXP XP to Level ${user.level + 1}",
                                    color = IDEColors.textMuted, fontSize = 11.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = IDEColors.accent, trackColor = IDEColors.bg3
                            )
                        }
                    }
                }
            }

            // ── Stats row ────────────────────────────────────────────────────
            item {
                val completedCourses = allProgress.values.count { it.isCompleted }
                val completedStages  = allProgress.values.sumOf { it.completedStageIds.size }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileStatCard(Modifier.weight(1f), "🎓", "$completedCourses", "Courses")
                    ProfileStatCard(Modifier.weight(1f), "📖", "$completedStages", "Stages")
                    ProfileStatCard(Modifier.weight(1f), "🏅", "${user.badges.size}", "Badges")
                    ProfileStatCard(Modifier.weight(1f), "📜", "${user.certificates.size}", "Certs")
                }
            }

            // ── Badges ───────────────────────────────────────────────────────
            if (user.badges.isNotEmpty()) {
                item {
                    Text("Badges Earned", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = IDEColors.textPrimary)
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(user.badges) { badgeId ->
                            val badge = CourseRepository.getBadge(badgeId)
                            if (badge != null) {
                                Column(
                                    modifier = Modifier.width(80.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(60.dp).clip(CircleShape)
                                            .background(Color(badge.color).copy(alpha = 0.2f))
                                            .border(2.dp, Color(badge.color), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) { Text(badge.icon, fontSize = 28.sp) }
                                    Text(badge.name, color = IDEColors.textSecondary,
                                        fontSize = 10.sp, textAlign = TextAlign.Center,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }

            // ── Certificates ─────────────────────────────────────────────────
            if (user.certificates.isNotEmpty()) {
                item {
                    Text("Certificates", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = IDEColors.textPrimary)
                }
                items(user.certificates) { certCourseId ->
                    val course = CourseRepository.getBasicCourse(certCourseId)
                        ?: CourseRepository.getAdvancedCourse(certCourseId)
                    val certTitle = course?.certificateTitle ?: certCourseId
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(IDEColors.bg2)
                            .border(1.dp,
                                IDEColors.green.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📜", fontSize = 28.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(certTitle, color = IDEColors.textPrimary, fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold)
                            Text("Certificate of Completion", color = IDEColors.textMuted,
                                fontSize = 12.sp)
                        }
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(IDEColors.green.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("✓ Earned", color = IDEColors.green, fontSize = 12.sp) }
                    }
                }
            }

            // ── Course progress ───────────────────────────────────────────────
            item {
                Text("Course Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    color = IDEColors.textPrimary)
            }
            val languages = listOf("kt","py","js","java","cpp","c","cs","rb","dart","vb")
            items(languages) { langId ->
                val basic = CourseRepository.getBasicCourse(langId) ?: return@items
                val adv   = CourseRepository.getAdvancedCourse(langId) ?: return@items
                val bPct  = courseVm.courseCompletionPercent(basic)
                val aPct  = courseVm.courseCompletionPercent(adv)
                if (bPct == 0 && aPct == 0) return@items

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(IDEColors.bg2)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(basic.icon, fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(basic.languageId.uppercase(), color = IDEColors.textPrimary,
                                fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Basic $bPct% · Adv $aPct%",
                                color = IDEColors.textMuted, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (bPct + aPct) / 200f },
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = IDEColors.accent, trackColor = IDEColors.bg4
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(60.dp)) }
        }
    }
}

@Composable
private fun ProfileStatCard(modifier: Modifier, icon: String, value: String, label: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(IDEColors.bg2)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Text(value, color = IDEColors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = IDEColors.textMuted, fontSize = 10.sp)
    }
}