create table member (
	`member_id` int,
	`member_name` char(50)
)ENGINE=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create table users (
	`user_name` char(50),
	`created_at` datetime,
	primary key(`user_name`)
)ENGINE=InnoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;