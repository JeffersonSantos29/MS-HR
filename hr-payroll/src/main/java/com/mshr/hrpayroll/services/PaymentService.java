package com.mshr.hrpayroll.services;

import org.springframework.stereotype.Service;

import com.mshr.hrpayroll.entities.Payment;

@Service
public class PaymentService {

	public Payment getPayment(long workerId, int days) {
		return new Payment ("Bob", 400.0, days );
	}
	
}
