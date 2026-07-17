-- MySQL 서버 하나에, 서비스별 스키마(데이터베이스) 3개만 분리
-- 로컬 MySQL 클라이언트 또는 Workbench에서 실행하세요

CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS performance_db;
