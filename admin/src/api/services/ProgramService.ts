import { ProgramRepository } from '../repositories/ProgramRepository';

export class ProgramService {
    static async list(search: string = '', page: number = 1) {
        const limit = 24;
        const offset = (page - 1) * limit;
        return await ProgramRepository.findAll(search, limit, offset);
    }

    static async getDetail(id: number) {
        const main: any = await ProgramRepository.findById(id);
        if (!main) return null;

        // Fetch sub-details through cleaner Repo methods
        const singers = await ProgramRepository.findSingers(id);
        const poets = await ProgramRepository.findPoets(id);
        const performers = await ProgramRepository.findPerformers(id);
        const timeline = await ProgramRepository.findTimeline(id);

        return {
            ...main,
            singers: singers.map(s => s.name),
            poets: poets.map(p => p.name),
            performers,
            timeline
        };
    }
}
