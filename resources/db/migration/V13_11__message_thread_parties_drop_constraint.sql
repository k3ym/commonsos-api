ALTER TABLE message_thread_parties DROP CONSTRAINT fk_message_thread_party_user_id;
ALTER TABLE message_thread_parties DROP CONSTRAINT fk_message_thread_party_message_thread_id;