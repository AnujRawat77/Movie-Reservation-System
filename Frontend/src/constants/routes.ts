export const ROUTES = {
  home: "/",
  movies: "/movies",
  movie: (id: string) => `/movies/${id}`,
  booking: (showtimeId: string) => `/booking/${showtimeId}`,
  bookingSuccess: (id: string) => `/booking/success/${id}`,
  myBookings: "/bookings",
  bookingDetail: (id: string) => `/bookings/${id}`,
  login: "/login",
  signup: "/signup",
  profile: "/profile",
} as const;
