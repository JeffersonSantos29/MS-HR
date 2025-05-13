package com.mshr.hrworker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mshr.hrworker.entities.Worker;

public interface WorkerRepository extends JpaRepository<Worker, Long>  {

}
