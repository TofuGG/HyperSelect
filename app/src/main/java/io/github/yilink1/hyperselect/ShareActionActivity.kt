package io.github.yilink1.hyperselect

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File

/** 系统分享：应用调出分享面板时的候选应用列表。 */
class ShareActionActivity : ManageListActivity() {

    override val screenTitle = "系统分享"
    override val emptyText = "暂无可用的分享应用"
    override val enablePinnedSection = true

    @Suppress("DEPRECATION")
    override fun queryTargets(pm: PackageManager): List<ResolveInfo> {
        val sendIntent = Intent(Intent.ACTION_SEND).setType("*/*")
        val sendMultipleIntent = Intent(Intent.ACTION_SEND_MULTIPLE).setType("*/*")
        return pm.queryIntentActivities(sendIntent, PackageManager.MATCH_ALL) +
            pm.queryIntentActivities(sendMultipleIntent, PackageManager.MATCH_ALL)
    }

    override fun blacklistOf(config: RuleConfig): Set<String> = config.shareBlacklist
    override fun orderOf(config: RuleConfig): List<String> = config.shareCustomOrder
    override fun withRules(config: RuleConfig, blacklist: Set<String>, order: List<String>): RuleConfig =
        config.copy(shareBlacklist = blacklist, shareCustomOrder = order)

    override fun testActions(): List<Pair<String, () -> Unit>> =
        listOf(Pair("测试效果", { showTestDialog() }))

    private fun showTestDialog() {
        val options = arrayOf("分享纯文本内容", "分享测试文本文件")
        AlertDialog.Builder(this)
            .setTitle("选择系统分享测试类型")
            .setItems(options) { _, which ->
                runCatching {
                    val sendIntent = if (which == 0) {
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "HyperSelect 分享测试文本：这是一条用于验证分享面板隐藏与排序效果的测试内容。")
                        }
                    } else {
                        val testFile = File(cacheDir, "hyper_share_test.txt").apply {
                            writeText("HyperSelect 分享文件测试：用于验证文件类分享目标应用的收纳与排序。")
                        }
                        val fileUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", testFile)
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }
                    startActivity(Intent.createChooser(sendIntent, "HyperSelect 分享效果测试"))
                }.onFailure { e ->
                    Toast.makeText(this, "调起分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}
