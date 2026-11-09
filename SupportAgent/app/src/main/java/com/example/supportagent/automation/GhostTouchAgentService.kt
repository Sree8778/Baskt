package com.example.supportagent.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class GhostTouchAgentService : AccessibilityService() {

    companion object {
        var instance: GhostTouchAgentService? = null
            private set

        val isServiceEnabled: Boolean
            get() = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d("GhostTouch", "Ghost Touch Accessibility Service Connected and Ready!")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Monitors window changes for reflection agent
    }

    override fun onInterrupt() {
        Log.w("GhostTouch", "Ghost Touch Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d("GhostTouch", "Ghost Touch Service Destroyed")
    }

    fun findAndClickAddToCart(): Boolean {
        val root = rootInActiveWindow ?: return false
        val candidateNodes = mutableListOf<AccessibilityNodeInfo>()

        searchNodes(root, candidateNodes) { node ->
            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""

            // Strict matching: Must be Add to Cart or Add to Order, NEVER Add to List or Save
            val isAddCart = text.contains("Add to cart", ignoreCase = true) ||
                    desc.contains("Add to cart", ignoreCase = true) ||
                    text.contains("Add to order", ignoreCase = true) ||
                    desc.contains("Add to order", ignoreCase = true) ||
                    text.equals("Add", ignoreCase = true) ||
                    (desc.startsWith("Add ", ignoreCase = true) && !desc.contains("list", ignoreCase = true))

            val isListOrSaved = text.contains("list", ignoreCase = true) ||
                    desc.contains("list", ignoreCase = true) ||
                    text.contains("save", ignoreCase = true) ||
                    desc.contains("save", ignoreCase = true) ||
                    text.contains("favorite", ignoreCase = true) ||
                    desc.contains("favorite", ignoreCase = true) ||
                    text.contains("registry", ignoreCase = true) ||
                    desc.contains("registry", ignoreCase = true) ||
                    desc.contains("heart", ignoreCase = true)

            isAddCart && !isListOrSaved
        }

        for (node in candidateNodes) {
            // Check if node itself is clickable
            if (node.isClickable) {
                if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.d("GhostTouch", "Direct clicked 'Add to cart'")
                    return true
                }
            }

            // Climb parent tree for clickable container
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    if (parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.d("GhostTouch", "Parent clicked 'Add to cart'")
                        return true
                    }
                }
                parent = parent.parent
            }

            // Fallback: Physical coordinate tap based on bounding box
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                val cx = rect.centerX().toFloat()
                val cy = rect.centerY().toFloat()
                Log.d("GhostTouch", "Physical tap on 'Add to cart' at ($cx, $cy)")
                return tapCoordinates(cx, cy)
            }
        }
        return false
    }

    fun findAndClick(targetText: String, ignoreCase: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val matchingNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodes(root, matchingNodes) { node ->
            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""
            text.contains(targetText, ignoreCase) || desc.contains(targetText, ignoreCase)
        }

        for (node in matchingNodes) {
            if (node.isClickable) {
                if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    Log.d("GhostTouch", "Direct clicked '$targetText'")
                    return true
                }
            }

            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    if (parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        Log.d("GhostTouch", "Parent clicked container for '$targetText'")
                        return true
                    }
                }
                parent = parent.parent
            }

            // Fallback: Physical coordinate tap based on bounding box
            val rect = Rect()
            node.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                val cx = rect.centerX().toFloat()
                val cy = rect.centerY().toFloat()
                Log.d("GhostTouch", "Physical bounds tap at ($cx, $cy) for '$targetText'")
                return tapCoordinates(cx, cy)
            }
        }
        return false
    }

    fun typeTextIntoField(targetHintOrText: String, textToType: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val candidateNodes = mutableListOf<AccessibilityNodeInfo>()
        searchNodes(root, candidateNodes) { node ->
            node.isEditable ||
            node.className?.contains("EditText", ignoreCase = true) == true ||
            node.text?.contains(targetHintOrText, ignoreCase = true) == true ||
            node.contentDescription?.contains(targetHintOrText, ignoreCase = true) == true
        }

        val target = candidateNodes.firstOrNull { it.isEditable } ?: candidateNodes.firstOrNull()
        if (target != null) {
            target.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val bundle = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            }
            val result = target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
            Log.d("GhostTouch", "Typed '$textToType' into editable field, result: $result")
            return result
        }
        return false
    }

    private fun searchNodes(
        root: AccessibilityNodeInfo,
        outList: MutableList<AccessibilityNodeInfo>,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ) {
        if (predicate(root)) {
            outList.add(root)
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            searchNodes(child, outList, predicate)
        }
    }

    fun tapCoordinates(x: Float, y: Float, durationMs: Long = 80): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    fun scroll(forward: Boolean = true): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return root.performAction(action)
    }

    fun pressBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun pressHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun getVisibleScreenText(): List<String> {
        val texts = mutableListOf<String>()
        val root = rootInActiveWindow ?: return texts
        extractNodeTexts(root, texts)
        return texts
    }

    private fun extractNodeTexts(node: AccessibilityNodeInfo, list: MutableList<String>) {
        val text = node.text?.toString() ?: node.contentDescription?.toString()
        if (!text.isNullOrBlank()) {
            list.add(text.trim())
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            extractNodeTexts(child, list)
        }
    }
}
