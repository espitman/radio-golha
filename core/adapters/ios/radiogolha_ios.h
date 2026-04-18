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
char* get_all_playlists_json(const char* user_db_path);
char* get_playlist_json(const char* user_db_path, int64_t id);
char* create_playlist_bridge(const char* user_db_path, const char* request_json);
char* rename_playlist_bridge(const char* user_db_path, int64_t id, const char* name);
char* delete_playlist_bridge(const char* user_db_path, int64_t id);
char* add_track_to_playlist_bridge(const char* user_db_path, int64_t playlist_id, int64_t track_id);
char* remove_track_from_playlist_bridge(const char* user_db_path, int64_t playlist_id, int64_t track_id);
char* get_manual_playlists_json(const char* user_db_path);
char* add_favorite_artist_bridge(const char* user_db_path, int64_t artist_id, const char* artist_type);
char* remove_favorite_artist_bridge(const char* user_db_path, int64_t artist_id);
char* is_favorite_artist_bridge(const char* user_db_path, int64_t artist_id);
char* get_favorite_artist_ids_json(const char* user_db_path, const char* artist_type);
char* record_playback_bridge(const char* user_db_path, int64_t track_id);
char* get_recently_played_ids_json(const char* user_db_path, int64_t limit);
char* get_most_played_ids_json(const char* user_db_path, int64_t limit);

void radiogolha_free_string(char* s);
