include $(top_srcdir)/config/utility.am

propsdir = @SIPX_CONFDIR@/imbot
dist_props_DATA = sipximbot-prompts.properties

imagedir = @SIPX_VARDIR@/sipximbot/image
dist_image_DATA = avatar.jpg

cfinputsdir = $(SIPX_CFINPUTS)/plugin.d
dist_cfinputs_DATA = \
	sipximbot.cf

EXTRA_DIST = \
	$(conf_DATA:=.in) \
	$(tmpfilesd_SCRIPTS:=.in)

confdir = @SIPX_CONFDIR@/sipximbot
conf_DATA = \
	log4j.properties

tmpfilesddir = /etc/tmpfiles.d
tmpfilesd_SCRIPTS = \
	sipximbot.conf

$(conf_DATA) $(tmpfilesd_SCRIPTS) : % : %.in Makefile
	@$(call SearchAndReplace,$<,$@)

CLEANFILES = $(conf_DATA) $(tmpfilesd_SCRIPTS)
