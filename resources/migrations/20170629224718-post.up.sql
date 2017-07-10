CREATE TABLE IF NOT EXISTS `posts` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `url` VARCHAR(255),
  `tags` VARCHAR(255),
  `content` VARCHAR(255),
  `title` VARCHAR(255),
  PRIMARY KEY `pk_id`(`id`)
);
