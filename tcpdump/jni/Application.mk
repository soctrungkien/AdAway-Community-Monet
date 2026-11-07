APP_ABI := all
APP_LDFLAGS += -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true
APP_SHORT_COMMANDS := true
