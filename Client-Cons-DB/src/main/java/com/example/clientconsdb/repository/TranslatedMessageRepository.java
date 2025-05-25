package com.example.clientconsdb.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clientconsdb.model.TranslatedMessage;

public interface TranslatedMessageRepository extends JpaRepository<TranslatedMessage, Long> {
}
