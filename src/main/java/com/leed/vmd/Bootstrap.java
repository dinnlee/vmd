package com.leed.vmd;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

public class Bootstrap
{

    public static void main( final String[] args )
    {
        final FormService formService = new FormService();

        staticFileLocation( "/public" );

        get( "/hello", ( req, res ) -> "Hello World" );

        post( "/complete-form", "application/json", ( request, response ) -> formService.complete( request.body() ),
                new JsonTransformer() );
    }

    public static Gson getGson()
    {
        return new GsonBuilder().create();
    }

    private static class JsonTransformer implements ResponseTransformer
    {
        @Override
        public String render( final Object model ) throws Exception
        {
            return Bootstrap.getGson().toJson( model );
        }
    }
}
