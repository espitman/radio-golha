export const topNavigation = [
  { label: "صفحه اصلی", to: "/" },
  { label: "خوانندگان", to: "/singers" },
  { label: "نوازندگان", to: "/players" },
  { label: "پلی لیست‌های من", to: "/playlists" },
  { label: "جستجوی پیشرفته", to: "/search" },
] as const;

export const sidebarNavigation = [
  { label: "خواننده‌های مورد علاقه", to: "/favorite-singers", icon: "☆" },
  { label: "نوازندگان مورد علاقه", to: "/favorite-players", icon: "♬" },
  { label: "محبوب‌ترین برنامه‌ها", to: "/popular", icon: "◇" },
  { label: "شنیده شده‌های اخیر", to: "/recent", icon: "◷" },
] as const;
