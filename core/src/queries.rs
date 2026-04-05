use rusqlite::{OptionalExtension, params};

use crate::{
    db::RadioGolhaCore,
    error::CoreResult,
    models::{
        ArtistListItem, ArtistListResponse, ArtistStats, CategoryOption, CategoryStat,
        DashboardOverview, DashboardSummary, LookupListItem, LookupListResponse, LookupStats,
        OrchestraLeaderCredit, PerformerCredit, ProgramDetail, ProgramListItem, ProgramListResponse,
        RankedNameStat, SingerOption, TimelineSegment, TranscriptVerse,
    },
};

#[derive(Debug, Clone, Copy)]
pub enum LookupKind {
    Orchestras,
    Instruments,
    Modes,
}

impl LookupKind {
    fn table_name(self) -> &'static str {
        match self {
            LookupKind::Orchestras => "orchestra",
            LookupKind::Instruments => "instrument",
            LookupKind::Modes => "mode",
        }
    }

    fn usage_sql(self) -> &'static str {
        match self {
            LookupKind::Orchestras => {
                "(SELECT COUNT(DISTINCT po.program_id) FROM program_orchestras po WHERE po.orchestra_id = base.id)"
            }
            LookupKind::Instruments => {
                "(SELECT COUNT(DISTINCT pp.program_id) FROM program_performers pp WHERE pp.instrument_id = base.id)"
            }
            LookupKind::Modes => {
                "(SELECT COUNT(DISTINCT pm.program_id) FROM program_modes pm WHERE pm.mode_id = base.id)"
            }
        }
    }
}

impl RadioGolhaCore {
    pub fn dashboard_summary(&self) -> CoreResult<DashboardSummary> {
        let summary = self.connection().query_row(
            "
            SELECT
              (SELECT COUNT(*) FROM program) AS total_programs,
              (SELECT COUNT(*) FROM artist) AS total_artists,
              (SELECT COUNT(*) FROM program_timeline) AS total_segments,
              (SELECT COUNT(*) FROM mode) AS total_modes,
              (SELECT COUNT(*) FROM program WHERE audio_url IS NOT NULL AND TRIM(audio_url) <> '') AS programs_with_audio,
              (SELECT COUNT(DISTINCT program_id) FROM program_timeline) AS programs_with_timeline,
              (SELECT COUNT(*) FROM category) AS total_categories,
              (SELECT COUNT(*) FROM orchestra) AS total_orchestras,
              (SELECT COUNT(*) FROM instrument) AS total_instruments
            ",
            [],
            |row| {
                Ok(DashboardSummary {
                    total_programs: row.get(0)?,
                    total_artists: row.get(1)?,
                    total_segments: row.get(2)?,
                    total_modes: row.get(3)?,
                    programs_with_audio: row.get(4)?,
                    programs_with_timeline: row.get(5)?,
                    total_categories: row.get(6)?,
                    total_orchestras: row.get(7)?,
                    total_instruments: row.get(8)?,
                })
            },
        )?;

        Ok(summary)
    }

    pub fn category_breakdown(&self) -> CoreResult<Vec<CategoryStat>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT c.title_fa, COUNT(*) AS total
            FROM program p
            JOIN category c ON c.id = p.category_id
            GROUP BY c.id
            ORDER BY total DESC, c.id ASC
            ",
        )?;

        let rows = stmt.query_map([], |row| {
            Ok(CategoryStat {
                name: row.get(0)?,
                total: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn top_singers(&self, limit: usize) -> CoreResult<Vec<RankedNameStat>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT a.name, COUNT(*) AS total
            FROM program_singers ps
            JOIN singer s ON s.id = ps.singer_id
            JOIN artist a ON a.id = s.artist_id
            GROUP BY s.id
            ORDER BY total DESC, a.name ASC
            LIMIT ?1
            ",
        )?;

        let rows = stmt.query_map([limit as i64], |row| {
            Ok(RankedNameStat {
                name: row.get(0)?,
                total: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn top_modes(&self, limit: usize) -> CoreResult<Vec<RankedNameStat>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT m.name, COUNT(*) AS total
            FROM program_modes pm
            JOIN mode m ON m.id = pm.mode_id
            GROUP BY m.id
            ORDER BY total DESC, m.name ASC
            LIMIT ?1
            ",
        )?;

        let rows = stmt.query_map([limit as i64], |row| {
            Ok(RankedNameStat {
                name: row.get(0)?,
                total: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn top_orchestras(&self, limit: usize) -> CoreResult<Vec<RankedNameStat>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT o.name, COUNT(*) AS total
            FROM program_orchestras po
            JOIN orchestra o ON o.id = po.orchestra_id
            GROUP BY o.id
            ORDER BY total DESC, o.name ASC
            LIMIT ?1
            ",
        )?;

        let rows = stmt.query_map([limit as i64], |row| {
            Ok(RankedNameStat {
                name: row.get(0)?,
                total: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn recent_programs(&self, limit: usize) -> CoreResult<Vec<ProgramListItem>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT p.id, p.title, c.title_fa, p.no, p.sub_no
            FROM program p
            JOIN category c ON c.id = p.category_id
            ORDER BY p.id DESC
            LIMIT ?1
            ",
        )?;

        let rows = stmt.query_map([limit as i64], |row| {
            Ok(ProgramListItem {
                id: row.get(0)?,
                title: row.get(1)?,
                category_name: row.get(2)?,
                no: row.get(3)?,
                sub_no: row.get(4)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn list_programs(&self, limit: usize, offset: usize) -> CoreResult<Vec<ProgramListItem>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT p.id, p.title, c.title_fa, p.no, p.sub_no
            FROM program p
            JOIN category c ON c.id = p.category_id
            ORDER BY p.no ASC, COALESCE(p.sub_no, '') ASC, p.id ASC
            LIMIT ?1 OFFSET ?2
            ",
        )?;

        let rows = stmt.query_map(params![limit as i64, offset as i64], |row| {
            Ok(ProgramListItem {
                id: row.get(0)?,
                title: row.get(1)?,
                category_name: row.get(2)?,
                no: row.get(3)?,
                sub_no: row.get(4)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn program_categories(&self) -> CoreResult<Vec<CategoryOption>> {
        let mut stmt = self
            .connection()
            .prepare("SELECT id, title_fa FROM category ORDER BY id ASC")?;

        let rows = stmt.query_map([], |row| {
            Ok(CategoryOption {
                id: row.get(0)?,
                title_fa: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn program_singers(&self) -> CoreResult<Vec<SingerOption>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT DISTINCT s.id, a.name
            FROM singer s
            JOIN artist a ON s.artist_id = a.id
            JOIN program_singers ps ON ps.singer_id = s.id
            ORDER BY a.name ASC
            ",
        )?;

        let rows = stmt.query_map([], |row| {
            Ok(SingerOption {
                id: row.get(0)?,
                name: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn count_programs(
        &self,
        search: &str,
        category_id: Option<i64>,
        singer_id: Option<i64>,
    ) -> CoreResult<i64> {
        let escaped_search = format!("%{}%", search.trim());
        let total = self.connection().query_row(
            "
            SELECT COUNT(*) as total
            FROM program p
            WHERE
              (?1 = '' OR p.title LIKE ?2 OR CAST(p.no AS TEXT) LIKE ?2 OR COALESCE(p.sub_no, '') LIKE ?2)
              AND (?3 IS NULL OR p.category_id = ?3)
              AND (
                ?4 IS NULL OR EXISTS (
                  SELECT 1
                  FROM program_singers ps
                  WHERE ps.program_id = p.id AND ps.singer_id = ?4
                )
              )
            ",
            params![search.trim(), escaped_search, category_id, singer_id],
            |row| row.get(0),
        )?;

        Ok(total)
    }

    pub fn list_programs_filtered(
        &self,
        search: &str,
        page: i64,
        category_id: Option<i64>,
        singer_id: Option<i64>,
        limit: i64,
    ) -> CoreResult<Vec<ProgramListItem>> {
        let page = page.max(1);
        let offset = (page - 1) * limit.max(1);
        let escaped_search = format!("%{}%", search.trim());
        let mut stmt = self.connection().prepare(
            "
            SELECT p.id, p.title, c.title_fa, p.no, p.sub_no
            FROM program p
            JOIN category c ON p.category_id = c.id
            WHERE
              (?1 = '' OR p.title LIKE ?2 OR CAST(p.no AS TEXT) LIKE ?2 OR COALESCE(p.sub_no, '') LIKE ?2)
              AND (?3 IS NULL OR p.category_id = ?3)
              AND (
                ?4 IS NULL OR EXISTS (
                  SELECT 1
                  FROM program_singers ps
                  WHERE ps.program_id = p.id AND ps.singer_id = ?4
                )
              )
            ORDER BY p.no ASC, COALESCE(p.sub_no, '') ASC, p.id ASC
            LIMIT ?5 OFFSET ?6
            ",
        )?;

        let rows = stmt.query_map(
            params![search.trim(), escaped_search, category_id, singer_id, limit.max(1), offset],
            |row| {
                Ok(ProgramListItem {
                    id: row.get(0)?,
                    title: row.get(1)?,
                    category_name: row.get(2)?,
                    no: row.get(3)?,
                    sub_no: row.get(4)?,
                })
            },
        )?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn admin_dashboard_overview(&self) -> CoreResult<DashboardOverview> {
        Ok(DashboardOverview {
            summary: self.dashboard_summary()?,
            category_breakdown: self.category_breakdown()?,
            top_singers: self.top_singers(8)?,
            top_modes: self.top_modes(8)?,
            top_orchestras: self.top_orchestras(5)?,
            recent_programs: self.recent_programs(6)?,
        })
    }

    pub fn admin_program_list(
        &self,
        search: &str,
        page: i64,
        category_id: Option<i64>,
        singer_id: Option<i64>,
    ) -> CoreResult<ProgramListResponse> {
        let limit = 24_i64;
        let safe_page = page.max(1);
        let total = self.count_programs(search, category_id, singer_id)?;
        let total_pages = ((total + limit - 1) / limit).max(1);

        Ok(ProgramListResponse {
            rows: self.list_programs_filtered(search, safe_page, category_id, singer_id, limit)?,
            categories: self.program_categories()?,
            singers: self.program_singers()?,
            total,
            page: safe_page,
            total_pages,
            active_category_id: category_id,
            active_singer_id: singer_id,
        })
    }

    pub fn artist_stats(&self) -> CoreResult<ArtistStats> {
        let stats = self.connection().query_row(
            "
            SELECT
              (SELECT COUNT(*) FROM artist) AS total_artists,
              (SELECT COUNT(*) FROM singer) AS singers,
              (SELECT COUNT(*) FROM performer) AS performers,
              (SELECT COUNT(*) FROM poet) AS poets
            ",
            [],
            |row| {
                Ok(ArtistStats {
                    total_artists: row.get(0)?,
                    singers: row.get(1)?,
                    performers: row.get(2)?,
                    poets: row.get(3)?,
                })
            },
        )?;

        Ok(stats)
    }

    pub fn count_artists(&self, search: &str, role: Option<&str>) -> CoreResult<i64> {
        let escaped_search = format!("%{}%", search.trim());
        let role = role.unwrap_or("").trim();
        let total = self.connection().query_row(
            "
            SELECT COUNT(*)
            FROM artist a
            WHERE
              (?1 = '' OR a.name LIKE ?2)
              AND (
                ?3 = ''
                OR (?3 = 'singer' AND EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id))
                OR (?3 = 'performer' AND EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id))
                OR (?3 = 'poet' AND EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id))
                OR (?3 = 'announcer' AND EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id))
                OR (?3 = 'composer' AND EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id))
                OR (?3 = 'arranger' AND EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id))
              )
            ",
            params![search.trim(), escaped_search, role],
            |row| row.get(0),
        )?;

        Ok(total)
    }

    pub fn list_artists_filtered(
        &self,
        search: &str,
        page: i64,
        role: Option<&str>,
        limit: i64,
    ) -> CoreResult<Vec<ArtistListItem>> {
        let page = page.max(1);
        let offset = (page - 1) * limit.max(1);
        let escaped_search = format!("%{}%", search.trim());
        let role = role.unwrap_or("").trim();
        let mut stmt = self.connection().prepare(
            "
            SELECT
              a.id,
              a.name,
              EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id) AS is_singer,
              EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id) AS is_performer,
              EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id) AS is_poet,
              EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id) AS is_announcer,
              EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id) AS is_composer,
              EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id) AS is_arranger
            FROM artist a
            WHERE
              (?1 = '' OR a.name LIKE ?2)
              AND (
                ?3 = ''
                OR (?3 = 'singer' AND EXISTS(SELECT 1 FROM singer s WHERE s.artist_id = a.id))
                OR (?3 = 'performer' AND EXISTS(SELECT 1 FROM performer p WHERE p.artist_id = a.id))
                OR (?3 = 'poet' AND EXISTS(SELECT 1 FROM poet po WHERE po.artist_id = a.id))
                OR (?3 = 'announcer' AND EXISTS(SELECT 1 FROM announcer an WHERE an.artist_id = a.id))
                OR (?3 = 'composer' AND EXISTS(SELECT 1 FROM composer c WHERE c.artist_id = a.id))
                OR (?3 = 'arranger' AND EXISTS(SELECT 1 FROM arranger ar WHERE ar.artist_id = a.id))
              )
            ORDER BY a.name COLLATE NOCASE ASC
            LIMIT ?4 OFFSET ?5
            ",
        )?;

        let rows = stmt.query_map(
            params![search.trim(), escaped_search, role, limit.max(1), offset],
            |row| {
                Ok(ArtistListItem {
                    id: row.get(0)?,
                    name: row.get(1)?,
                    is_singer: row.get(2)?,
                    is_performer: row.get(3)?,
                    is_poet: row.get(4)?,
                    is_announcer: row.get(5)?,
                    is_composer: row.get(6)?,
                    is_arranger: row.get(7)?,
                })
            },
        )?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn admin_artist_list(&self, search: &str, page: i64, role: Option<&str>) -> CoreResult<ArtistListResponse> {
        let limit = 24_i64;
        let safe_page = page.max(1);
        let normalized_role = role.map(str::trim).filter(|value| !value.is_empty());
        let total = self.count_artists(search, normalized_role)?;
        let total_pages = ((total + limit - 1) / limit).max(1);

        Ok(ArtistListResponse {
            rows: self.list_artists_filtered(search, safe_page, normalized_role, limit)?,
            stats: self.artist_stats()?,
            total,
            page: safe_page,
            total_pages,
            active_role: normalized_role.map(ToOwned::to_owned),
        })
    }

    pub fn count_lookup_items(&self, kind: LookupKind, search: &str) -> CoreResult<i64> {
        let escaped_search = format!("%{}%", search.trim());
        let sql = format!(
            "SELECT COUNT(*) FROM {} base WHERE (?1 = '' OR base.name LIKE ?2)",
            kind.table_name()
        );
        let total = self
            .connection()
            .query_row(&sql, params![search.trim(), escaped_search], |row| row.get(0))?;
        Ok(total)
    }

    pub fn list_lookup_items(
        &self,
        kind: LookupKind,
        search: &str,
        page: i64,
        limit: i64,
    ) -> CoreResult<Vec<LookupListItem>> {
        let page = page.max(1);
        let offset = (page - 1) * limit.max(1);
        let escaped_search = format!("%{}%", search.trim());
        let sql = format!(
            "
            SELECT
              base.id,
              base.name,
              {} AS usage_count
            FROM {} base
            WHERE (?1 = '' OR base.name LIKE ?2)
            ORDER BY base.name COLLATE NOCASE ASC
            LIMIT ?3 OFFSET ?4
            ",
            kind.usage_sql(),
            kind.table_name()
        );
        let mut stmt = self.connection().prepare(&sql)?;
        let rows = stmt.query_map(params![search.trim(), escaped_search, limit.max(1), offset], |row| {
            Ok(LookupListItem {
                id: row.get(0)?,
                name: row.get(1)?,
                usage_count: row.get(2)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    pub fn lookup_stats(&self, kind: LookupKind) -> CoreResult<LookupStats> {
        let sql = format!(
            "
            SELECT
              (SELECT COUNT(*) FROM {table}) AS total_items,
              (SELECT COALESCE(SUM(usage_count), 0) FROM (
                SELECT {usage} AS usage_count
                FROM {table} base
              )) AS total_usage
            ",
            table = kind.table_name(),
            usage = kind.usage_sql()
        );
        let stats = self.connection().query_row(&sql, [], |row| {
            Ok(LookupStats {
                total_items: row.get(0)?,
                total_usage: row.get(1)?,
            })
        })?;

        Ok(stats)
    }

    pub fn admin_lookup_list(&self, kind: LookupKind, search: &str, page: i64) -> CoreResult<LookupListResponse> {
        let limit = 24_i64;
        let safe_page = page.max(1);
        let total = self.count_lookup_items(kind, search)?;
        let total_pages = ((total + limit - 1) / limit).max(1);

        Ok(LookupListResponse {
            rows: self.list_lookup_items(kind, search, safe_page, limit)?,
            stats: self.lookup_stats(kind)?,
            total,
            page: safe_page,
            total_pages,
        })
    }

    pub fn get_program_detail(&self, program_id: i64) -> CoreResult<Option<ProgramDetail>> {
        let base = self
            .connection()
            .query_row(
                "
                SELECT p.id, p.title, c.title_fa, p.no, p.sub_no, p.audio_url
                FROM program p
                JOIN category c ON c.id = p.category_id
                WHERE p.id = ?1
                ",
                [program_id],
                |row| {
                    Ok(ProgramDetail {
                        id: row.get(0)?,
                        title: row.get(1)?,
                        category_name: row.get(2)?,
                        no: row.get(3)?,
                        sub_no: row.get(4)?,
                        audio_url: row.get(5)?,
                        singers: Vec::new(),
                        poets: Vec::new(),
                        announcers: Vec::new(),
                        composers: Vec::new(),
                        arrangers: Vec::new(),
                        modes: Vec::new(),
                        orchestras: Vec::new(),
                        orchestra_leaders: Vec::new(),
                        performers: Vec::new(),
                        timeline: Vec::new(),
                        transcript: Vec::new(),
                    })
                },
            )
            .optional()?;

        let Some(mut detail) = base else {
            return Ok(None);
        };

        detail.singers = self.simple_name_list(
            "
            SELECT a.name
            FROM program_singers ps
            JOIN singer s ON s.id = ps.singer_id
            JOIN artist a ON a.id = s.artist_id
            WHERE ps.program_id = ?1
            ORDER BY a.name ASC
            ",
            program_id,
        )?;
        detail.poets = self.simple_name_list(
            "
            SELECT a.name
            FROM program_poets pp
            JOIN poet p ON p.id = pp.poet_id
            JOIN artist a ON a.id = p.artist_id
            WHERE pp.program_id = ?1
            ORDER BY a.name ASC
            ",
            program_id,
        )?;
        detail.announcers = self.simple_name_list(
            "
            SELECT a.name
            FROM program_announcers pa
            JOIN announcer an ON an.id = pa.announcer_id
            JOIN artist a ON a.id = an.artist_id
            WHERE pa.program_id = ?1
            ORDER BY a.name ASC
            ",
            program_id,
        )?;
        detail.composers = self.simple_name_list(
            "
            SELECT a.name
            FROM program_composers pc
            JOIN composer c ON c.id = pc.composer_id
            JOIN artist a ON a.id = c.artist_id
            WHERE pc.program_id = ?1
            ORDER BY a.name ASC
            ",
            program_id,
        )?;
        detail.arrangers = self.simple_name_list(
            "
            SELECT a.name
            FROM program_arrangers pa
            JOIN arranger ar ON ar.id = pa.arranger_id
            JOIN artist a ON a.id = ar.artist_id
            WHERE pa.program_id = ?1
            ORDER BY a.name ASC
            ",
            program_id,
        )?;
        detail.modes = self.simple_name_list(
            "
            SELECT DISTINCT m.name
            FROM program_modes pm
            JOIN mode m ON m.id = pm.mode_id
            WHERE pm.program_id = ?1
            ORDER BY m.name ASC
            ",
            program_id,
        )?;
        detail.orchestras = self.simple_name_list(
            "
            SELECT DISTINCT o.name
            FROM program_orchestras po
            JOIN orchestra o ON o.id = po.orchestra_id
            WHERE po.program_id = ?1
            ORDER BY o.name ASC
            ",
            program_id,
        )?;
        detail.orchestra_leaders = self.program_orchestra_leaders(program_id)?;
        detail.performers = self.program_performers(program_id)?;
        detail.timeline = self.program_timeline(program_id)?;
        detail.transcript = self.program_transcript(program_id)?;

        Ok(Some(detail))
    }

    fn program_transcript(&self, program_id: i64) -> CoreResult<Vec<TranscriptVerse>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT segment_order, verse_order, text
            FROM program_transcript_verses
            WHERE program_id = ?1
            ORDER BY segment_order ASC, verse_order ASC, id ASC
            ",
        )?;

        let rows = stmt.query_map([program_id], |row| {
            Ok(TranscriptVerse {
                segment_order: row.get(0)?,
                verse_order: row.get(1)?,
                text: row.get(2)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn simple_name_list(&self, sql: &str, program_id: i64) -> CoreResult<Vec<String>> {
        let mut stmt = self.connection().prepare(sql)?;
        let rows = stmt.query_map([program_id], |row| row.get::<_, String>(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn program_orchestra_leaders(&self, program_id: i64) -> CoreResult<Vec<OrchestraLeaderCredit>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT DISTINCT o.name, a.name
            FROM program_orchestra_leaders pol
            JOIN orchestra o ON o.id = pol.orchestra_id
            JOIN orchestra_leader ol ON ol.id = pol.orchestra_leader_id
            JOIN artist a ON a.id = ol.artist_id
            WHERE pol.program_id = ?1
            ORDER BY o.name ASC, a.name ASC
            ",
        )?;

        let rows = stmt.query_map([program_id], |row| {
            Ok(OrchestraLeaderCredit {
                orchestra: row.get(0)?,
                leader: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn program_performers(&self, program_id: i64) -> CoreResult<Vec<PerformerCredit>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT a.name, i.name
            FROM program_performers pp
            JOIN performer p ON p.id = pp.performer_id
            JOIN artist a ON a.id = p.artist_id
            LEFT JOIN instrument i ON i.id = pp.instrument_id
            WHERE pp.program_id = ?1
            ORDER BY a.name ASC
            ",
        )?;

        let rows = stmt.query_map([program_id], |row| {
            Ok(PerformerCredit {
                name: row.get(0)?,
                instrument: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn program_timeline(&self, program_id: i64) -> CoreResult<Vec<TimelineSegment>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT
              t.id,
              t.start_time,
              t.end_time,
              COALESCE(
                (
                  SELECT GROUP_CONCAT(m2.name, '، ')
                  FROM program_timeline_modes ptm
                  JOIN mode m2 ON m2.id = ptm.mode_id
                  WHERE ptm.timeline_id = t.id
                ),
                m.name
              ) AS mode_name
            FROM program_timeline t
            LEFT JOIN mode m ON m.id = t.mode_id
            WHERE t.program_id = ?1
            ORDER BY t.id ASC
            ",
        )?;

        let base_segments = stmt.query_map([program_id], |row| {
            Ok((
                row.get::<_, i64>(0)?,
                row.get::<_, Option<String>>(1)?,
                row.get::<_, Option<String>>(2)?,
                row.get::<_, Option<String>>(3)?,
            ))
        })?;

        let mut segments = Vec::new();
        for segment in base_segments {
            let (timeline_id, start_time, end_time, mode_name) = segment?;
            segments.push(TimelineSegment {
                id: timeline_id,
                start_time,
                end_time,
                mode_name,
                singers: self.timeline_name_list(
                    "
                    SELECT a.name
                    FROM program_timeline_singers pts
                    JOIN singer s ON s.id = pts.singer_id
                    JOIN artist a ON a.id = s.artist_id
                    WHERE pts.timeline_id = ?1
                    ORDER BY a.name ASC
                    ",
                    timeline_id,
                )?,
                poets: self.timeline_name_list(
                    "
                    SELECT a.name
                    FROM program_timeline_poets ptp
                    JOIN poet p ON p.id = ptp.poet_id
                    JOIN artist a ON a.id = p.artist_id
                    WHERE ptp.timeline_id = ?1
                    ORDER BY a.name ASC
                    ",
                    timeline_id,
                )?,
                announcers: self.timeline_name_list(
                    "
                    SELECT a.name
                    FROM program_timeline_announcers pta
                    JOIN announcer an ON an.id = pta.announcer_id
                    JOIN artist a ON a.id = an.artist_id
                    WHERE pta.timeline_id = ?1
                    ORDER BY a.name ASC
                    ",
                    timeline_id,
                )?,
                orchestras: self.timeline_name_list(
                    "
                    SELECT DISTINCT o.name
                    FROM program_timeline_orchestras pto
                    JOIN orchestra o ON o.id = pto.orchestra_id
                    WHERE pto.timeline_id = ?1
                    ORDER BY o.name ASC
                    ",
                    timeline_id,
                )?,
                orchestra_leaders: self.timeline_orchestra_leaders(timeline_id)?,
                performers: self.timeline_performers(timeline_id, program_id)?,
            });
        }

        Ok(segments)
    }

    fn timeline_name_list(&self, sql: &str, timeline_id: i64) -> CoreResult<Vec<String>> {
        let mut stmt = self.connection().prepare(sql)?;
        let rows = stmt.query_map([timeline_id], |row| row.get::<_, String>(0))?;
        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn timeline_orchestra_leaders(&self, timeline_id: i64) -> CoreResult<Vec<OrchestraLeaderCredit>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT DISTINCT o.name, a.name
            FROM program_timeline_orchestra_leaders ptol
            JOIN orchestra o ON o.id = ptol.orchestra_id
            JOIN orchestra_leader ol ON ol.id = ptol.orchestra_leader_id
            JOIN artist a ON a.id = ol.artist_id
            WHERE ptol.timeline_id = ?1
            ORDER BY o.name ASC, a.name ASC
            ",
        )?;

        let rows = stmt.query_map([timeline_id], |row| {
            Ok(OrchestraLeaderCredit {
                orchestra: row.get(0)?,
                leader: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }

    fn timeline_performers(&self, timeline_id: i64, program_id: i64) -> CoreResult<Vec<PerformerCredit>> {
        let mut stmt = self.connection().prepare(
            "
            SELECT a.name, i.name
            FROM program_timeline_performers ptp
            JOIN performer p ON p.id = ptp.performer_id
            JOIN artist a ON a.id = p.artist_id
            LEFT JOIN program_performers pp
              ON pp.program_id = ?2 AND pp.performer_id = ptp.performer_id
            LEFT JOIN instrument i ON i.id = pp.instrument_id
            WHERE ptp.timeline_id = ?1
            ORDER BY a.name ASC
            ",
        )?;

        let rows = stmt.query_map(params![timeline_id, program_id], |row| {
            Ok(PerformerCredit {
                name: row.get(0)?,
                instrument: row.get(1)?,
            })
        })?;

        rows.collect::<Result<Vec<_>, _>>().map_err(Into::into)
    }
}
