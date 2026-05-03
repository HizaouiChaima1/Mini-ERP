import { useState, useCallback } from 'react'

interface LoadingState {
    loading: boolean
    error: string | null
}

export const useAsync = <T>(asyncFunction: () => Promise<T>) => {
    const [state, setState] = useState<LoadingState>({
        loading: false,
        error: null,
    })

    const execute = useCallback(async () => {
        setState({ loading: true, error: null })
        try {
            const response = await asyncFunction()
            setState({ loading: false, error: null })
            return response
        } catch (error: unknown) {
            let message = 'Une erreur est survenue'
            if (error instanceof Error && error.message) {
                message = error.message
            }
            if (error && typeof error === 'object' && 'response' in error) {
                const data = (error as { response?: { data?: { message?: unknown } } }).response?.data
                if (data?.message !== undefined && data.message !== null) {
                    message = String(data.message)
                }
            }
            setState({ loading: false, error: message })
            throw error
        }
    }, [asyncFunction])

    return { ...state, execute }
}
