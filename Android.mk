ifneq ($(BOARD_USE_DEFAULT_APPINSTALL),false)
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := BluetoothRemote
LOCAL_CERTIFICATE := platform

ifndef PRODUCT_SHIPPING_API_LEVEL
LOCAL_PRIVATE_PLATFORM_APIS := true
else
LOCAL_SDK_VERSION := current
endif

include $(BUILD_PACKAGE)
endif
