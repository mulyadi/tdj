NAME
====
tdj - run Teradata queries through JDBC

SYNOPSIS
========
    % java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H hostname -u username -p password [-o file] [-d delimiter] [-f format] [-r] sql

DESCRIPTION
===========
tdj is a command line Java program that runs query on Teradata and
prints the result onto standard input or a file.
It needs args4j and Teradata JDBC jar files to run. args4j jar file can be
downloaded from http://args4j.kohsuke.org/. Teradata JDBC jar files
(tdgssconfig.jar, terajdbc4.jar) can be downloaded from Teradata website.

Type the following to run the program:

java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H hostname -u username -p password [-o file] [-d delimiter] [-f format] [-r] sql

COMMAND LINE ARGUMENTS
======================
### Required
    -u, --hostname  connect to this hostname (default localhost)
    -u, --username  connect as this username
    -p, --password  connect with this password
    sql             the SQL to run

### Optional
    -h, --help      display this help text and exit
    -o, --output    write output to this file (default is write to stdout)
    -d, --delimiter character to be used as delimiter, override format option (default tab)
    -f, --format    format text, csv (default tab-separated text)
    -r, --header    print column names

INSTALLATION
============
- Download and install Java application launcher. Type _whereis java_ to find where your Java application is.
- Download Teradata JDBC files. These files need to be listed in the classpath.
- Download args4j jar file from http://args4j.kohsuke.org/.
- If you want to compile the source code, download and install Java compiler, and run _javac -classpath .:args4j-2.0.21.jar tdj_.
- You can download the Teradata JDBC files here: http://downloads.teradata.com/download/connectivity/jdbc-driver
- Put the class and jar files in the same folder to try the sample runs listed in the example section.

PLANNED IMPROVEMENTS
====================
- Create executable jar file.

EXAMPLES
========
    % java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H tdhostname -u johndoe -p mypassword 'select current_timestamp'
    % java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H tdhostname -u johndoe -p mypassword -o current_date.txt -d "|" -r 'select current_date'
    % java -classpath .:args4j-2.0.21.jar:terajdbc4.jar:tdgssconfig.jar tdj -H tdhostname -u johndoe -p mypassword -o current_date.txt -f text -r 'select current_date'

AUTHOR
======
Mulyadi Kurniawan

