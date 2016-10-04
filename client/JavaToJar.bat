"c:\Program Files\Java\jdk1.8.0_92\bin\javac" -encoding UTF-8 MainClass.java
md jar
"c:\Program Files\Java\jdk1.8.0_92\bin\jar" cvfe jar\Chat-client.jar MainClass *.class
erase /F /Q *.class
pause