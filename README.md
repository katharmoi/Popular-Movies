# Popular_Movies_Stage_1
Popular Movies Stage 1
Adding API key
1.Add
buildTypes.each {
        it.buildConfigField 'String', 'MOVIE_DB_API_KEY',MyMovieDbApiKey
    }
to build.gradle
2.Add your themoviedb Api key to gradle.properties as MYAPIKEY

