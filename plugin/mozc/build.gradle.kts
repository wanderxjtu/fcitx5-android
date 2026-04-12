plugins {
    id("org.fcitx.fcitx5.android.app-convention")
    id("org.fcitx.fcitx5.android.plugin-app-convention")
    id("org.fcitx.fcitx5.android.native-app-convention")
    id("org.fcitx.fcitx5.android.build-metadata")
    id("org.fcitx.fcitx5.android.data-descriptor")
    id("org.fcitx.fcitx5.android.fcitx-component")
}

android {
    namespace = "org.fcitx.fcitx5.android.plugin.mozc"

    defaultConfig {
        applicationId = "org.fcitx.fcitx5.android.plugin.mozc"

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                targets("mozc")
            }
        }
    }

    buildTypes {
        release {
            resValue("string", "app_name", "@string/app_name_release")
            isMinifyEnabled = true
            isShrinkResources = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
        debug {
            resValue("string", "app_name", "@string/app_name_debug")
        }
    }

    packaging {
        jniLibs {
            excludes += setOf(
                "**/libc++_shared.so",
                "**/libFcitx5*"
            )
        }
    }
    androidResources {
        // noCompress 现在是一个 MutableSet<String> 属性
        // 使用 += 或 addAll 来添加需要排除压缩的扩展名或后缀
        noCompress += listOf("conf", "so", "bin", "dict")
    }
    buildFeatures {
        // 显式启用 resValues 功能
        resValues = true
    }
}

fcitxComponent {
    installPrebuiltAssets = true
}

dependencies {
    implementation(project(":lib:fcitx5"))
    implementation(project(":lib:plugin-base"))
}
