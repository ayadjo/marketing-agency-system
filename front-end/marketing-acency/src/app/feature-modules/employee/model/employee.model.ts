import { User } from "src/app/infrastructure/auth/model/user.model";

export interface Employee {
    id: number;
    firstName?: string | null;
    lastName?: string | null;
    address?: string | null;
    city?: string | null;
    country?: string | null;
    phoneNumber?: string | null;
    user: User;
}

