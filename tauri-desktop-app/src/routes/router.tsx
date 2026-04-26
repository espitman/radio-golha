import { createRootRoute, createRoute, createRouter } from "@tanstack/react-router";
import { AppShell } from "../components/layout/AppShell";
import { HomePage } from "../features/home/HomePage";
import { SearchPage } from "../features/search/SearchPage";
import { SingersPage } from "../features/singers/SingersPage";
import { PlayersPage } from "../features/players/PlayersPage";
import { SettingsPage } from "../features/settings/SettingsPage";
import { ArtistDetailsPage } from "../features/artists/ArtistDetailsPage";
import { ProgramDetailsPage } from "../features/programs/ProgramDetailsPage";
import { TrackDetailsPage } from "../features/tracks/TrackDetailsPage";

function PlaceholderPage({ title }: { title: string }) {
  return (
    <section className="pt-3 text-right">
      <h1 className="text-4xl font-black tracking-[-0.05em]">{title}</h1>
      <p className="mt-4 text-sm font-bold text-golha-muted">این route آماده است و در فاز بعدی به دیتابیس و کامپوننت‌های اصلی وصل می‌شود.</p>
    </section>
  );
}

const rootRoute = createRootRoute({ component: AppShell });

const indexRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: "/",
  component: HomePage,
});

const routes = [
  indexRoute,
  createRoute({ getParentRoute: () => rootRoute, path: "/singers", component: SingersPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/players", component: PlayersPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/playlists", component: () => <PlaceholderPage title="پلی لیست‌های من" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/programs/$programId", component: ProgramDetailsPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/tracks/$trackId", component: TrackDetailsPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/favorite-singers", component: () => <PlaceholderPage title="خواننده‌های مورد علاقه" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/favorite-players", component: () => <PlaceholderPage title="نوازندگان مورد علاقه" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/popular", component: () => <PlaceholderPage title="محبوب‌ترین برنامه‌ها" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/recent", component: () => <PlaceholderPage title="شنیده شده‌های اخیر" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/search", component: SearchPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/settings", component: SettingsPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/help", component: () => <PlaceholderPage title="راهنما" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/archive", component: () => <PlaceholderPage title="آرشیو" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/favorites", component: () => <PlaceholderPage title="برگزیده‌ها" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/collections", component: () => <PlaceholderPage title="مجموعه‌های خاص" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/researchers", component: () => <PlaceholderPage title="پژوهشگران" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/artists", component: () => <PlaceholderPage title="هنرمندان" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/artists/$artistId", component: ArtistDetailsPage }),
  createRoute({ getParentRoute: () => rootRoute, path: "/modes", component: () => <PlaceholderPage title="دستگاه‌ها" /> }),
  createRoute({ getParentRoute: () => rootRoute, path: "/poets", component: () => <PlaceholderPage title="شاعران" /> }),
];

const routeTree = rootRoute.addChildren(routes);

export const router = createRouter({ routeTree });

declare module "@tanstack/react-router" {
  interface Register {
    router: typeof router;
  }
}
