#include <stdint.h>

char* get_home_feed_json(const char* db_path);
char* get_top_tracks_json(const char* db_path);
char* get_singers_json(const char* db_path);
char* get_musicians_json(const char* db_path);
char* get_artist_detail_json(const char* db_path, int64_t artist_id);
char* get_programs_by_category_json(const char* db_path, int64_t category_id);
char* get_program_detail_json(const char* db_path, int64_t program_id);
char* get_orchestras_json(const char* db_path);
char* get_programs_by_orchestra_json(const char* db_path, int64_t orchestra_id);
char* get_programs_by_ids_json(const char* db_path, const char* ids_json);
char* get_duet_pairs_config_json(const char* db_path);
char* get_programs_by_mode_json(const char* db_path, int64_t mode_id);
char* get_ordered_modes_json(const char* db_path);
char* get_config_json(const char* db_path, const char* key);
char* get_search_options_json(const char* db_path);
char* search_programs_json(const char* db_path, const char* filters_json);
char* get_duet_programs_json(const char* db_path, const char* singer1, const char* singer2);

void radiogolha_free_string(char* s);
