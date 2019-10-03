//package com.lowes.storeelasticsearch.config;
//
//import java.io.IOException;
//
//import org.springframework.classify.Classifier;
//import org.springframework.retry.RetryPolicy;
//import org.springframework.retry.policy.AlwaysRetryPolicy;
//import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
//import org.springframework.retry.policy.NeverRetryPolicy;
//
///**
// * Custom class to Retry Kafka Messages on Exception from ElasticSearch DB
// * 
// * @author ndevara
// *
// */
//public class CustomExceptionRetryPolicy extends ExceptionClassifierRetryPolicy {
//	private static final long serialVersionUID = 1L;
//
//	@SuppressWarnings("serial")
//	public CustomExceptionRetryPolicy() {
//		final AlwaysRetryPolicy alwaysRetryPolicy = new AlwaysRetryPolicy();
//		this.setExceptionClassifier(new Classifier<Throwable, RetryPolicy>() {
//			@Override
//			public RetryPolicy classify(Throwable classifiable) {
//
//				/**
//				 * If there is IOException from ElasticSearch Transaction (like CRUD Operations
//				 * in Indexes) then Retry Policy kicks in and the Message is retried
//				 */
//				if (classifiable.getCause() instanceof IOException) {
//					return alwaysRetryPolicy;
//				}
//				return new NeverRetryPolicy();
//			}
//		});
//	}
//}
