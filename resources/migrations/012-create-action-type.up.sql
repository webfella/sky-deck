create table action_type (
   id uuid primary key default uuid_generate_v1mc(),
   name varchar(256)
);
