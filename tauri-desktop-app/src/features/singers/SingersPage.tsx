import { Link } from "@tanstack/react-router";

type SingerCard = {
  name: string;
  count: string;
  image: string;
  alt: string;
};

const alphabet = ["همه", "ی", "ه", "و", "ن", "م", "ق", "ع", "ش", "س", "ز", "ر", "د", "خ", "ح", "چ", "ج", "ت", "پ", "ب", "الف"];

const singers: SingerCard[] = [
  {
    name: "محمدرضا شجریان",
    count: "۱۲۰ برنامه ثبت شده",
    alt: "portrait of Mohammad-Reza Shajarian",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAWAuv8u0v1OIdyaGEUsfEcihpkz_8B_2gjKaFA66dCpN-Bqm_q4O4aJtGXvZ_0sInBsWn3IeZK0MlQA3l3x1RaiBvHutcVq9wu7H3UxxIJfFUpoqyl_Srw8haM4QQvjRRSHmLVYdboxoZeraXwY6SZucALPrPCdVuwEU5aNdf4mXG7Qwymhth2R1d4g90KlYTTsV3t0IUtD6Rlzg-C8o6AR9RmrXkr11rSrhha2h2uWhGvc6hILiGtcl7JdaCpPvx0APRgcdUscMPr",
  },
  {
    name: "بانو مرضیه",
    count: "۸۵ برنامه ثبت شده",
    alt: "portrait of Marzieh",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuACrGUSeZphvMCbjU0pye8xCe_PoOpzcMulvXk3mHMDKbA3OPCh63S7Aan_HtY6ayCTALorUGT3Z-wU9diP_81YzbfI1u98A80KxnG54vIRcXU0-ysHEXJ-kJJ_S3AxSG27fRfR2cN1vP1VWZIoQI1Kz-zyVjHl9E5crKA0OupEphOwxh_kiUFRyfyrv3py1STGV3Mp_O3JA6O4Web1Pu3U0BO1irzC7NuVwUaJuUR3zz1ipmZ_JsGbMO6Uit47YeaVb-fDS8QMTLcY",
  },
  {
    name: "غلامحسین بنان",
    count: "۹۴ برنامه ثبت شده",
    alt: "portrait of Banan",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuA7uzrhoj_jfgLEXEqe_zHCIBmD2nK7-Hu4ebRr9XEuIgj0FjXvjDsaxw6XEr9_AbHIU6FnIUgl8L8Yvn5F1pOJlK860RIOHTB4iq1OGwvbiPwPJM8CF__oLq3j0iJmWfzrpAKzwTBh1HEkuW_kjpE8UgxZpJYilRpvDh2v4DXxD_pBqYiV1REoplA8Xs1HhUHLvtO9GedBX4znf3_Kojkqzl_SJ7c_KEC_HGH2o946wPNz5Cy89q-WpPIwbOZ9Ue7Ex_34FrGLs1Xc",
  },
  {
    name: "بانو دلکش",
    count: "۷۲ برنامه ثبت شده",
    alt: "portrait of Delkash",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAyMXtTSRs5UqZMcg51Kxydy5fF2sQHZAFszIOcUBUqZPdvqJjiwNY2kNPAluK1GBXCFc9CTB2XY06W9DCxrlvwodZOw44gPK-RwHjOoeD3Uoj7H8ooUMUsiab4sGaPqMDJDmJM8Il6IJ7HOo17XtQVcVAdfXKAc3G8bagN2UZxoNN2D7MYL1dj82NYQfp8Qt7QsU7wvTupq9FxwT08lhYnGsctnfQ8vr39s6rap1GJURZDRqzwRDbKuofgcOaeojycQpuwDd38JCCz",
  },
  {
    name: "جلیل شهناز",
    count: "۱۱۵ برنامه ثبت شده",
    alt: "portrait of Jalil Shahnaz",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAnrG5u3f3e7Z6v_7u_X9O9M_N1_Z2W7R8A0Q5D8k3_I0u5J1D2F3G4H5J6K7L8M9N0P1Q2R3S4T5U6V7W8X9Y0Z1A2B3C4D5E6F7G8H9I0J1K2L3M4N5O6P7Q8R9S0T1U2V3W4X5Y6Z7A8B9C0",
  },
  {
    name: "پرویز یاحقی",
    count: "۱۰۸ برنامه ثبت شده",
    alt: "portrait of Parviz Yahaghi",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAsf9g8h7j6k5l4m3n2o1p0q9r8s7t6u5v4w3x2y1z0a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4a3b2c1d0e9f8g7h6i5j4k3l2m1n0o9p8q7r6s5t4u3v2w1",
  },
  {
    name: "همایون شجریان",
    count: "۴۵ برنامه ثبت شده",
    alt: "portrait of Homayoun Shajarian",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAq8w7e6r5t4y3u2i1o0p9l8k7j6h5g4f3d2s1a0q9w8e7r6t5y4u3i2o1p0l9k8j7h6g5f4d3s2a1q0w9e8r7t6y5u4i3o2p1l0k9j8h7g6f5d4s3a2q1w0",
  },
  {
    name: "بانو الهه",
    count: "۶۸ برنامه ثبت شده",
    alt: "portrait of Banu Elahe",
    image:
      "https://lh3.googleusercontent.com/aida-public/AB6AXuAm4n5b6v7c8x9z0q1w2e3r4t5y6u7i8o9p0l1k2j3h4g5f6d7s8a9q0w1e2r3t4y5u6i7o8p9l0k1j2h3g4f5d6s7a8q9w0e1r2t3y4u5i6o7p8l9k0",
  },
];

function SingerCardView({ singer }: { singer: SingerCard }) {
  return (
    <Link
      to="/artists/$artistId"
      params={{ artistId: singer.name }}
      className="group relative block overflow-hidden rounded-xl border border-outline-variant/10 bg-surface-container-lowest transition-all duration-500 hover:shadow-xl"
    >
      <div className="relative aspect-square overflow-hidden">
        <img
          alt={singer.alt}
          className="h-full w-full object-cover grayscale transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
          src={singer.image}
        />
        <div className="absolute inset-0 flex items-end bg-gradient-to-t from-primary/80 via-transparent to-transparent p-6 opacity-0 transition-opacity duration-500 group-hover:opacity-100">
          <span className="block w-full rounded-full bg-secondary-container px-6 py-2 text-center text-xs font-bold text-on-secondary-container">مشاهده آثار</span>
        </div>
      </div>
      <div className="p-5 text-right">
        <h3 className="mb-1 text-xl font-bold text-primary">{singer.name}</h3>
        <p className="text-sm font-medium text-on-surface-variant">{singer.count}</p>
      </div>
    </Link>
  );
}

export function SingersPage() {
  return (
    <div className="px-6 pb-32 pt-8 md:px-12">
      <div className="mb-12 text-right">
        <h1 className="mb-2 text-5xl font-black text-primary">خوانندگان</h1>
        <p className="mr-auto max-w-2xl text-on-surface-variant leading-relaxed">
          فهرست جامع اساتید، خوانندگان و نوازندگان تاریخ رادیو گلها به ترتیب حروف الفبا.
        </p>
      </div>

      <div className="no-scrollbar mb-12 overflow-x-auto">
        <div className="flex min-w-max flex-row-reverse gap-2 pb-4">
          {alphabet.map((letter, index) => (
            <button
              key={letter}
              className={
                index === 0
                  ? "rounded-full bg-primary px-4 py-2 text-sm font-bold text-on-primary shadow-md"
                  : "rounded-full bg-surface-container px-4 py-2 text-sm font-bold text-primary transition-colors hover:bg-secondary-container"
              }
            >
              {letter}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
        {singers.map((singer) => (
          <SingerCardView key={singer.name} singer={singer} />
        ))}
      </div>
    </div>
  );
}
