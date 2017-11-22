# searchFilesFromContent
get a list files with content based on list fo words taken as input

build a maven project/import to eclipse and click on export as war
deploy to tomcat
http://localhost:8080/searchFilesFromContent/rest/filecontents?match=private  or
http://localhost:8080/searchFilesFromContent/rest/filecontents
with body 
{
"match":["private", "girl"]
}
