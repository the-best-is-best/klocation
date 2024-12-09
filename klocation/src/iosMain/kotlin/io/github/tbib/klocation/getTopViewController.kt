package io.github.tbib.klocation

import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController

// Helper function to get the current view controller
fun getTopViewController(): UIViewController? {
    val window = UIApplication.sharedApplication.keyWindow
    return getTopViewControllerFromRoot(window?.rootViewController)
}

// Recursive function to traverse the view controller hierarchy
fun getTopViewControllerFromRoot(rootViewController: UIViewController?): UIViewController? {
    if (rootViewController == null) return null

    if (rootViewController.presentedViewController != null) {
        return getTopViewControllerFromRoot(rootViewController.presentedViewController)
    }

    if (rootViewController is UINavigationController) {
        return getTopViewControllerFromRoot(rootViewController.visibleViewController)
    }

    if (rootViewController is UITabBarController) {
        return getTopViewControllerFromRoot(rootViewController.selectedViewController)
    }

    return rootViewController
}
