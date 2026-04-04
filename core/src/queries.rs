use rusqlite::{OptionalExtension, params};

use crate::{
    db::RadioGolhaCore,
    error::CoreResult,
    models::{
        CategoryStat, DashboardSummary, OrchestraLeaderCredit, PerformerCredit, ProgramDetail,
        ProgramListItem, RankedNameStat, TimelineSegment,
    },
};

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
              (SELECT COUNT(DISTINCT program_id) FROM program_timeline) AS programs_with_timeline
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

        Ok(Some(detail))
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
