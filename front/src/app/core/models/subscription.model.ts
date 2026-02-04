export interface Subscription {
    userId: number;
    topicId: number;
}

// La clé unique est (userId, topicId). Ce modèle représente l'abonnement d'un utilisateur à un topic.
