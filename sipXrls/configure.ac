AC_PREREQ(2.57)
AC_INIT(sipXrls, 19.08, sipx-dev@list.sipfoundry.org)
AC_CONFIG_AUX_DIR(config)
m4_include([config/general.m4])
m4_include([config/sipXlib.m4])
AM_INIT_AUTOMAKE(foreign tar-ustar)
SFAC_AUTOMAKE_VERSION([1.6])
AM_CONFIG_HEADER([src/config.h])
AC_PROG_LIBTOOL
AC_PATH_PROG([BASH], [bash])
AC_PATH_PROG([PERL], [perl])
AC_PROG_CXX
AX_COMPILER_VENDOR
AC_PROG_CC
CHECK_XARGS_REPLACE
SFAC_INIT_FLAGS
SFAC_LIB_COMMSERVER
SFAC_LIB_CALL
SFAC_LIB_MEDIA
SFAC_LIB_STACK
CHECK_GIPSNEQ
AC_ARG_WITH([dailydir],
  AC_HELP_STRING([--with-dailydir=directory],
    [The directory containing scripts that are run every night]),
  [DAILYDIR=${withval}],[DAILYDIR='$(sysconfdir)/cron.daily'])
AC_SUBST([DAILYDIR])
CHECK_POSTGRES
CHECK_SERVICEDIR
AC_CONFIG_FILES([
	Makefile
	src/Makefile
	src/test/Makefile
	etc/Makefile
	bin/Makefile
	meta/Makefile
])
AC_OUTPUT
