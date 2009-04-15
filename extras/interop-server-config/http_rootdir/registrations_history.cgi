#! /usr/bin/perl

use CGI qw/:standard/;
use XML::Parser;
use strict;

# The un-escape table for backslash escapes.
my(%unescape) = ("r", "\r",
		 "n", "\n",
		 "\"", "\"",
		 "\\", "\\");

# Read and process the registrar log files.
my(@registrations);
my(%contacts_seen);
my(%gruu);
# Read at least 24 hours of log data in chronological order.
&process_log_file('@SIPX_LOGDIR@/sipregistrar.log.1');
&process_log_file('@SIPX_LOGDIR@/sipregistrar.log');

# Generate the table body in order by extension.
my($table_body) = '';
my($registration);
foreach $registration (sort registration_cmp @registrations) {
    # Ignore registrations that have expired.
    my($AOR) = $registration->{'AOR'};
    my($extension) = $AOR =~ /sips?:(\d+)@/;
    $table_body .= 
	&Tr({-align => 'left'},
	    &td([
		 &show($AOR, 1),
		 &show($registration->{'contact'}, 1),
		 &show($registration->{'user_agent'}, 1),
		 &show($registration->{'q'}, 0),
		 &show($registration->{'instance_id'}, 1),
		 &show($gruu{$AOR, $registration->{'instance_id'}}, 1),
		 &show($registration->{'path'}, 1)])
		 &show($registration->{'callid'}, 1)])
	    ) . "\n"
}

# Start the HTML.
print &header,
    &start_html('Registration History'), "\n",
    &h1('Registration History'), "\n";

# Beware that <tr> is generated by the Tr() function, because tr is a
# keyword.
print &p(&table({-border => 1},
		&Tr({-align => 'left'},
		    &th([
			 'AOR',
			 'Contact',
			 'User-Agent',
			 'q',
			 'Instance ID',
			 'GRUU',
			 'Path',
			 'Call-Id'
			 ])), "\n",
		$table_body)),
    "\n";

# Print the boilerplate.
print <<EOF;
<p>"AOR" is the address of record for which the contact is registered.<br/>
"Contact" is the contact address which is registered.<br/>
"User-Agent" is the value from the User-Agent header in the REGISTER.<br/>
"q" is the q-value which shows the preference of this contact for this AOR
(1.0 = highest, 0.0 = lowest).<br/>
"Instance ID" is the <code>+sip.instance</code> given for this contact.
<!--
Registrations for extensions <code>1gg7</code>, <code>1gg8</code>, and
<code>1gg9</code> will have their <code>+sip.instance</code> values removed
and so will not be assigned GRUUs.
-->
<br/>
"GRUU" is the GRUU that was assigned for this contact.
A GRUU will only be assigned if a <code>+sip.instance</code> was provided.<br/>
"Path" represents the content of the Path header(s) contained in the 
REGISTER message.<br/>
"Call-Id" is the Call-Id of the REGISTER request that established this
registration.
Multiple registrations for the same contact with different
Call-Id values show that the UA is incorrectly using different Call-Id values
for successive REGISTERs.<br/>
EOF

# End the HTML.
print &end_html,
    "\n";

exit 0;

# Extract the (top-level) text content from an XML tree.
sub text {
    my($tree) = @_;
    my($text) = '';
    my $i;
    for ($i = 1; $i < $#$tree; $i += 2) {
	if (${$tree}[$i] eq '0') {
	    $text .= ${$tree}[$i+1];
        }
    }
    return $text;
}

# Function to compare two registration hashes.
sub registration_cmp {
    # Extract the AOR fields and from them extract the extension number.
    my($a_ext) = $a->{'AOR'} =~ /sip:(\d+)\@/;
    my($b_ext) = $b->{'AOR'} =~ /sip:(\d+)\@/;
    # Compare the extensions numerically.
    return $a_ext <=> $b_ext;
}

# Extract the user-agent info from a log file.
sub process_log_file {
    my($log_file) = @_;
    my($contact, $extension, $user_agent, $call_id, $aor, $i,
       $instance_id, $gruu, $path);

    # Read through the log file and find all the REGISTERs.
    my($log_line) = '';
    open(LOG, $log_file) ||
	die "Error opening file '$log_file' for input: $!\n";
    while (<LOG>) {
	if (/:INCOMING:/ &&
	    m%----\\nREGISTER %i) {
	    # This line is a REGISTER request, process it.

	    # Normalize the log line.
	    s/^.*?----\\n//;
	    s/====*END====*\\n"\n$//;
	    s/\\(.)/$unescape{$1}/eg;
	    s/\r\n/\n/g;

	    # Get the contact.
	    ($contact) = /\nContact:\s*(.*)\n/i;
	    # Remove the expires parameter.
	    $contact =~ s/;expires=\d+//i;

	    # Check to see if we've seen this contact before.
	    next if defined($contacts_seen{$contact});
	    $contacts_seen{$contact} = 1;

	    # Get the remaining data items.
	    my($registration) = {};
	    $registration->{'contact'} = $contact;
	    ($registration->{'extension'}) = /\nTo:.*?sips?:(\d+)@/i;
	    ($registration->{'user_agent'}) = /\nUser-Agent:\s*(.*)\n/i;
	    ($registration->{'callid'}) = /\nCall-ID:\s*(.*)\n/i;
	    ($registration->{'path'}) = /\nPath:\s*(.*)\n/i;
	    ($registration->{'AOR'}) = /\nTo:\s*(.*)\n/i;
	    ($registration->{'q'}) = $contact =~ /;q=([0-9.]+)/;
	    ($i) = $contact =~ /;\+sip\.instance=([^;]*)/i;
            # Remove quotes on instance ID.
	    $i = $1 if $i =~ /^"(.*)"$/;
	    $registration->{'instance_id'} = $i;

	    # Remove the to-tag.
	    $registration->{'AOR'} =~ s/;tag=[^;]+//;

	    # Save the values.
	    push(@registrations, $registration);	
        } elsif (/:OUTGOING:/ &&
             m%----\\nSIP/2\.0 200 %i &&
             /\\nCseq:[ 0-9]*REGISTER\\r/i) {
            # This line is a REGISTER response, process it.

	    # Normalize the log line.
	    s/^.*?----\\n//;
	    s/====*END====*\\n"\n$//;
	    s/\\(.)/$unescape{$1}/eg;
	    s/\r\n/\n/g;

	    # Get the contact.
	    ($contact) = /\nContact:\s*(.*)\n/i;
	    # Remove the expires parameter.
	    $contact =~ s/;expires=\d+//i;

	    # Get the instance ID.
	    ($instance_id) = $contact =~ /;\+sip\.instance=([^;]*)/i;
            # Remove quotes on instance ID.
	    $instance_id = $1 if $instance_id =~ /^"(.*)"$/;

	    # Get the AOR.
	    ($aor) = /\nTo:\s*(.*)\n/i;
	    # Remove the to-tag.
	    $aor =~ s/;tag=[^;]+//;

	    # Get the Path.
	    ($path) = /\nPath:\s*(.*)\n/i;

	    # Get the GRUU.
	    ($gruu) = $contact =~ /;gr=([^;]*)/i;
            # Remove quotes on GRUU.
	    $gruu = $1 if $gruu =~ /^"(.*)"$/;

	    # Remember the GRUU.
	    $gruu{$aor, $instance_id} = $gruu
		if $instance_id ne '' && $gruu ne '';
        }
    }
    close LOG;
}

# Turn a string into the representation we want for it in the table:
# Escape HTML special characters.
# Put <wbr/> tags between individual characters, so fields fold, if requested
# by second argument.
# Replace empty strings with &nbsp; so their borders how in IE.
# Put into code font, if requested by second argument.
sub show {
    my($string, $code) = @_;
    my($r);

    if ($string eq '') {
	$r = "&nbsp;";
    } else {
	my(@chars) = split(//, $string);
	@chars = map { $_ eq '<' ? '&lt;' :
			   $_ eq '>' ? '&gt;' :
			   $_ eq '&' ? '&amp;' :
			   $_ eq ' ' ? '&nbsp;' :
			   $_ } @chars;
	$r = $code ? &code(join('<wbr/>', @chars)) : join('', @chars);
    }
    return $r;
}
