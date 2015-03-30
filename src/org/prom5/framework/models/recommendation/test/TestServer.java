package org.prom5.framework.models.recommendation.test;

import java.io.IOException;

import org.prom5.framework.models.recommendation.RecommendationProvider;
import org.prom5.framework.models.recommendation.net.RecommendationServiceHandler;
import org.prom5.framework.remote.Service;

public class TestServer {

	public static void main(String args[]) {
		RecommendationProvider provider = new TestProvider();
		RecommendationServiceHandler handler = new RecommendationServiceHandler(provider);
		Service svc = new Service(4444, handler);
		try {
			System.out.print("Starting server...");
			svc.start();
			System.out.println(" running!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
